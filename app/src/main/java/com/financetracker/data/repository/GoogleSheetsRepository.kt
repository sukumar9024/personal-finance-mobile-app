package com.financetracker.data.repository

import android.content.Context
import android.util.Log
import com.financetracker.data.model.Expense
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class GoogleSheetsRepository(private val context: Context) {
    companion object {
        private const val TAG = "GoogleSheetsRepo"
        private const val SPREADSHEET_ID = BuildConfig.SPREADSHEET_ID
        private const val EXPENSES_PREFIX = "expenses_"
        private const val CATEGORIES_SHEET = "categories"
        private const val BUDGETS_SHEET = "budgets"
        private const val SUMMARIES_SHEET = "summaries"

        // Headers for expenses sheet
        private val EXPENSE_HEADERS = listOf(
            "Date", "Amount", "Category", "Subcategory", "Description",
            "Payment Method", "Receipt URL", "Tags", "Created At", "Modified At"
        )
    }

    private val sheetsService: Sheets by lazy {
        createSheetsService()
    }

    private fun createSheetsService(): Sheets {
        val credential = if (BuildConfig.SERVICE_ACCOUNT_JSON.isNotEmpty()) {
            // Use service account JSON from build config
            try {
                val json = BuildConfig.SERVICE_ACCOUNT_JSON
                val inputStream = ByteArrayInputStream(json.toByteArray(StandardCharsets.UTF_8))
                GoogleCredential.fromStream(inputStream)
                    .createScoped(listOf(SheetsScopes.SPREADSHEETS))
            } catch (e: Exception) {
                Log.e(TAG, "Error creating credential from BuildConfig", e)
                throw e
            }
        } else {
            // Fallback: Try to load from assets
            val inputStream = context.assets.open("service-account-key.json")
            GoogleCredential.fromStream(inputStream)
                .createScoped(listOf(SheetsScopes.SPREADSHEETS))
        }
        
        return Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("FinanceTracker")
            .setReadTimeout(30000) // 30 seconds
            .setConnectTimeout(30000)
            .build()
    }

    suspend fun getCurrentMonthSheetName(): String {
        val now = LocalDate.now()
        return "${EXPENSES_PREFIX}${now.year}_${String.format("%02d", now.monthValue)}"
    }

    suspend fun ensureMonthSheetExists(sheetName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = sheetsService.spreadsheets().get(SPREADSHEET_ID)
            val response = request.execute()
            val existingSheets = response.sheets.map { it.properties.title }
            
            if (sheetName !in existingSheets) {
                createSheet(sheetName)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring sheet exists: $sheetName", e)
            false
        }
    }

    private fun createSheet(sheetName: String) {
        val requestBody = RequestBody()
            .addRequests(
                listOf(
                    Request()
                        .addProperties(RequestProperties().setTitle(sheetName)),
                    Request()
                        .addUpdateCells(
                            UpdateCellsRequest()
                                .setStart(
                                    GridCoordinate()
                                        .setSheetId(0)
                                        .setRowIndex(0)
                                        .setColumnIndex(0)
                                )
                                .setRows(
                                    listOf(
                                        RowData().setValues(
                                            EXPENSE_HEADERS.map { header ->
                                                CellData().setUserEnteredValue(
                                                    ExtendedValue().setStringValue(header)
                                                )
                                            }
                                        )
                                    )
                                )
                                .setFields("userEnteredValue")
                        )
                )
            )

        val response = sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, requestBody).execute()
        Log.d(TAG, "Created sheet: $sheetName with headers")
    }

    suspend fun fetchExpenses(sheetName: String): List<Expense> = withContext(Dispatchers.IO) {
        try {
            val range = "$sheetName!A2:J" // Skip header, get all data columns A-J
            val response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute()
            
            val values = response.values ?: emptyList()
            values.mapIndexed { index, row ->
                mapRowToExpense(row, index + 2) // Row index starts at 2 (after header)
            }.filter { it.date != null }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching expenses from $sheetName", e)
            emptyList()
        }
    }

    private fun mapRowToExpense(row: List<Any>, sheetRowIndex: Int): Expense {
        try {
            val date = LocalDate.parse(row.getOrElse(0) { "" }.toString())
            val amount = row.getOrElse(1) { "0.0" }.toString().toDoubleOrNull() ?: 0.0
            val category = row.getOrElse(2) { "" }.toString()
            val subcategory = row.getOrElse(3) { "" }.toString().takeIf { it.isNotBlank() }
            val description = row.getOrElse(4) { "" }.toString()
            val paymentMethod = row.getOrElse(5) { "Cash" }.toString()
            val receiptUrl = row.getOrElse(6) { "" }.toString().takeIf { it.isNotBlank() }
            val tags = row.getOrElse(7) { "" }.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
            
            val createdAt = parseDateTime(row.getOrElse(8) { "" }.toString())
            val modifiedAt = parseDateTime(row.getOrElse(9) { "" }.toString())

            return Expense(
                date = date,
                amount = amount,
                category = category,
                subcategory = subcategory,
                description = description,
                paymentMethod = paymentMethod,
                receiptUrl = receiptUrl,
                tags = tags,
                createdAt = createdAt,
                modifiedAt = modifiedAt,
                sheetRowIndex = sheetRowIndex
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping row to expense: $row", e)
            return Expense(
                date = LocalDate.now(),
                amount = 0.0,
                category = "",
                sheetRowIndex = sheetRowIndex
            )
        }
    }

    private fun parseDateTime(value: String): java.time.LocalDateTime {
        return if (value.isNotBlank()) {
            try {
                java.time.LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            } catch (e: Exception) {
                java.time.LocalDateTime.now()
            }
        } else {
            java.time.LocalDateTime.now()
        }
    }

    suspend fun addExpense(sheetName: String, expense: Expense): Boolean = withContext(Dispatchers.IO) {
        try {
            val row = mapExpenseToRow(expense)
            val valueRange = ValueRange()
                .setValues(listOf(row))

            sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, "$sheetName!A:J", valueRange)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute()

            Log.d(TAG, "Added expense to sheet $sheetName")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding expense to $sheetName", e)
            false
        }
    }

    suspend fun updateExpense(sheetName: String, expense: Expense): Boolean = withContext(Dispatchers.IO) {
        try {
            if (expense.sheetRowIndex <= 0) {
                Log.e(TAG, "Invalid sheet row index: ${expense.sheetRowIndex}")
                return@withContext false
            }

            val row = mapExpenseToRow(expense)
            val valueRange = ValueRange()
                .setValues(listOf(row))

            val range = "$sheetName!A${expense.sheetRowIndex}:J${expense.sheetRowIndex}"
            sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, range, valueRange)
                .setValueInputOption("RAW")
                .execute()

            Log.d(TAG, "Updated expense at row ${expense.sheetRowIndex}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating expense at row ${expense.sheetRowIndex}", e)
            false
        }
    }

    suspend fun deleteExpense(sheetName: String, rowIndex: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            if (rowIndex <= 0) {
                Log.e(TAG, "Invalid row index: $rowIndex")
                return@withContext false
            }

            val requestBody = RequestBody()
                .addRequests(
                    listOf(
                        Request()
                            .setDeleteDimension(
                                DeleteDimensionRequest()
                                    .setRange(
                                        DimensionRange()
                                            .setSheetId(getSheetIdByName(sheetName))
                                            .setDimension("ROWS")
                                            .setStartIndex(rowIndex - 1) // 0-indexed
                                            .setEndIndex(rowIndex)
                                    )
                            )
                    )
                )

            sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, requestBody).execute()
            Log.d(TAG, "Deleted row $rowIndex from sheet $sheetName")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting row $rowIndex from $sheetName", e)
            false
        }
    }

    private fun getSheetIdByName(sheetName: String): Int {
        try {
            val response = sheetsService.spreadsheets().get(SPREADSHEET_ID).execute()
            val sheet = response.sheets.find { it.properties.title == sheetName }
            return sheet?.properties?.sheetId ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sheet ID for $sheetName", e)
            return 0
        }
    }

    private fun mapExpenseToRow(expense: Expense): List<Any> {
        return listOf(
            expense.date.toString(),
            expense.amount.toString(),
            expense.category,
            expense.subcategory ?: "",
            expense.description,
            expense.paymentMethod,
            expense.receiptUrl ?: "",
            expense.tags.joinToString(", "),
            expense.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            expense.modifiedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }

    suspend fun fetchCategories(): List<Category> = withContext(Dispatchers.IO) {
        try {
            val range = "$CATEGORIES_SHEET!A2:C"
            val response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute()
            
            response.values?.map { row ->
                Category(
                    name = row.getOrElse(0) { "" }.toString(),
                    color = row.getOrElse(1) { "#FF6200EE" }.toString(),
                    monthlyBudget = row.getOrElse(2) { null }?.toString()?.toDoubleOrNull()
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching categories", e)
            getDefaultCategories()
        }
    }

    private fun getDefaultCategories(): List<Category> {
        return listOf(
            Category("Food", "#FF5722", "restaurant", 10000.0),
            Category("Transport", "#2196F3", "directions_car", 5000.0),
            Category("Shopping", "#E91E63", "shopping_bag", 8000.0),
            Category("Bills", "#9C27B0", "receipt", 15000.0),
            Category("Entertainment", "#FF9800", "movie", 5000.0),
            Category("Health", "#4CAF50", "local_hospital", 3000.0),
            Category("Education", "#3F51B5", "school", 2000.0),
            Category("Other", "#607D8B", "more_horiz", 0.0)
        )
    }

    suspend fun fetchMonthlySummary(sheetName: String): Map<String, Double> = withContext(Dispatchers.IO) {
        val expenses = fetchExpenses(sheetName)
        val summary = mutableMapOf<String, Double>()
        
        expenses.groupBy { it.category }.forEach { (category, categoryExpenses) ->
            summary[category] = categoryExpenses.sumOf { it.amount }
        }
        
        summary
    }
}