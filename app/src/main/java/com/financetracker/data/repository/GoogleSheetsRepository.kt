package com.financetracker.data.repository

import android.content.Context
import com.financetracker.BuildConfig
import com.financetracker.data.model.Category
import com.financetracker.data.model.Expense
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime

class GoogleSheetsRepository(private val context: Context) {
    companion object {
        private const val CATEGORIES_SHEET = "categories"
        private const val SERVICE_ACCOUNT_ASSET = "service-account-key.json"

        private val DEFAULT_CATEGORIES = listOf(
            Category("Food", "#FF5722", "restaurant", 10000.0),
            Category("Transport", "#2196F3", "directions_car", 5000.0),
            Category("Shopping", "#E91E63", "shopping_bag", 8000.0),
            Category("Bills", "#9C27B0", "receipt", 15000.0),
            Category("Entertainment", "#FF9800", "movie", 5000.0),
            Category("Health", "#4CAF50", "local_hospital", 3000.0),
            Category("Education", "#3F51B5", "school", 2000.0),
            Category("Other", "#607D8B", "more_horiz", 0.0)
        )

        private val EXPENSE_HEADERS = listOf(
            "Date",
            "Amount",
            "Category",
            "Subcategory",
            "Description",
            "Payment Method",
            "Receipt URL",
            "Tags",
            "Created At",
            "Modified At"
        )

        private val CATEGORY_HEADERS = listOf("Name", "Color", "Monthly Budget")
    }

    private val transport = NetHttpTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val spreadsheetId = BuildConfig.SPREADSHEET_ID.trim()

    fun getCurrentMonthSheetName(): String {
        val now = LocalDate.now()
        return "expenses_${now.year}_${String.format("%02d", now.monthValue)}"
    }

    fun isReadyForLiveSync(): Boolean {
        return spreadsheetId.isNotBlank() && hasCredentials()
    }

    fun getConfigurationStatusMessage(): String? {
        return when {
            spreadsheetId.isBlank() -> "Spreadsheet ID is missing in build.gradle.kts."
            !hasCredentials() -> "Spreadsheet ID is set, but service-account-key.json is missing in app/src/main/assets."
            else -> null
        }
    }

    suspend fun fetchExpenses(sheetName: String): List<Expense> = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext emptyList()
        ensureExpenseSheet(service, sheetName)

        val values = service.spreadsheets().values()
            .get(spreadsheetId, "$sheetName!A2:J")
            .execute()
            .getValues()
            .orEmpty()

        values.mapIndexed { index, row ->
            Expense(
                id = "row_${index + 2}",
                date = parseDate(row.valueAt(0)),
                amount = row.valueAt(1).toDoubleOrNull() ?: 0.0,
                category = row.valueAt(2),
                subcategory = row.valueAt(3).ifBlank { null },
                description = row.valueAt(4),
                paymentMethod = row.valueAt(5).ifBlank { "Cash" },
                receiptUrl = row.valueAt(6).ifBlank { null },
                tags = row.valueAt(7)
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() },
                createdAt = parseDateTime(row.valueAt(8)),
                modifiedAt = parseDateTime(row.valueAt(9)),
                sheetRowIndex = index + 2
            )
        }
    }

    suspend fun addExpense(sheetName: String, expense: Expense): Boolean = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext false
        ensureExpenseSheet(service, sheetName)

        val row = listOf(
            expense.date.toString(),
            expense.amount.toString(),
            expense.category,
            expense.subcategory.orEmpty(),
            expense.description,
            expense.paymentMethod,
            expense.receiptUrl.orEmpty(),
            expense.tags.joinToString(","),
            expense.createdAt.toString(),
            expense.modifiedAt.toString()
        )

        service.spreadsheets().values()
            .append(spreadsheetId, "$sheetName!A:J", ValueRange().setValues(listOf(row)))
            .setValueInputOption("RAW")
            .execute()

        true
    }

    suspend fun updateExpense(sheetName: String, expense: Expense): Boolean = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext false
        if (expense.sheetRowIndex <= 1) return@withContext false

        val updatedRow = listOf(
            expense.date.toString(),
            expense.amount.toString(),
            expense.category,
            expense.subcategory.orEmpty(),
            expense.description,
            expense.paymentMethod,
            expense.receiptUrl.orEmpty(),
            expense.tags.joinToString(","),
            expense.createdAt.toString(),
            LocalDateTime.now().toString()
        )

        service.spreadsheets().values()
            .update(
                spreadsheetId,
                "$sheetName!A${expense.sheetRowIndex}:J${expense.sheetRowIndex}",
                ValueRange().setValues(listOf(updatedRow))
            )
            .setValueInputOption("RAW")
            .execute()

        true
    }

    suspend fun deleteExpense(sheetName: String, expenseId: String): Boolean = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext false
        val rowIndex = expenseId.removePrefix("row_").toIntOrNull() ?: return@withContext false
        val sheetId = ensureExpenseSheet(service, sheetName).sheetId ?: return@withContext false

        val deleteRequest = Request().setDeleteDimension(
            DeleteDimensionRequest().setRange(
                DimensionRange()
                    .setSheetId(sheetId)
                    .setDimension("ROWS")
                    .setStartIndex(rowIndex - 1)
                    .setEndIndex(rowIndex)
            )
        )

        service.spreadsheets().batchUpdate(
            spreadsheetId,
            BatchUpdateSpreadsheetRequest().setRequests(listOf(deleteRequest))
        ).execute()

        true
    }

    suspend fun fetchCategories(): List<Category> = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext DEFAULT_CATEGORIES
        ensureCategoriesSheet(service)

        val rows = service.spreadsheets().values()
            .get(spreadsheetId, "$CATEGORIES_SHEET!A2:C")
            .execute()
            .getValues()
            .orEmpty()

        if (rows.isEmpty()) {
            seedDefaultCategories(service)
            return@withContext DEFAULT_CATEGORIES
        }

        rows.map { row ->
            Category(
                name = row.valueAt(0),
                color = row.valueAt(1).ifBlank { "#607D8B" },
                icon = "default",
                monthlyBudget = row.valueAt(2).toDoubleOrNull()
            )
        }
    }

    suspend fun fetchMonthlySummary(sheetName: String): Map<String, Double> = withContext(Dispatchers.IO) {
        fetchExpenses(sheetName)
            .groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }

    private fun getSheetsService(): Sheets? {
        if (spreadsheetId.isBlank()) return null
        val credentialStream = openCredentialStream() ?: return null

        credentialStream.use { input ->
            val credential = GoogleCredential.fromStream(input, transport, jsonFactory)
            val scopedCredential = if (credential.createScopedRequired()) {
                credential.createScoped(listOf(SheetsScopes.SPREADSHEETS))
            } else {
                credential
            }

            return Sheets.Builder(transport, jsonFactory, scopedCredential)
                .setApplicationName("FinanceTracker")
                .build()
        }
    }

    private fun hasCredentials(): Boolean {
        return BuildConfig.SERVICE_ACCOUNT_JSON.isNotBlank() || hasAssetCredential()
    }

    private fun hasAssetCredential(): Boolean {
        return runCatching {
            context.assets.open(SERVICE_ACCOUNT_ASSET).use { true }
        }.getOrDefault(false)
    }

    private fun openCredentialStream(): InputStream? {
        return when {
            BuildConfig.SERVICE_ACCOUNT_JSON.isNotBlank() -> {
                ByteArrayInputStream(BuildConfig.SERVICE_ACCOUNT_JSON.toByteArray(StandardCharsets.UTF_8))
            }
            hasAssetCredential() -> context.assets.open(SERVICE_ACCOUNT_ASSET)
            else -> null
        }
    }

    private fun ensureExpenseSheet(service: Sheets, sheetName: String): SheetProperties {
        return ensureSheetExists(service, sheetName, EXPENSE_HEADERS)
    }

    private fun ensureCategoriesSheet(service: Sheets): SheetProperties {
        return ensureSheetExists(service, CATEGORIES_SHEET, CATEGORY_HEADERS)
    }

    private fun ensureSheetExists(
        service: Sheets,
        sheetName: String,
        headers: List<String>
    ): SheetProperties {
        getSheetProperties(service, sheetName)?.let { return it }

        val addSheetRequest = Request().setAddSheet(
            AddSheetRequest().setProperties(
                SheetProperties().setTitle(sheetName)
            )
        )

        service.spreadsheets().batchUpdate(
            spreadsheetId,
            BatchUpdateSpreadsheetRequest().setRequests(listOf(addSheetRequest))
        ).execute()

        service.spreadsheets().values()
            .append(spreadsheetId, "$sheetName!A1", ValueRange().setValues(listOf(headers)))
            .setValueInputOption("RAW")
            .execute()

        return requireNotNull(getSheetProperties(service, sheetName))
    }

    private fun seedDefaultCategories(service: Sheets) {
        val rows = DEFAULT_CATEGORIES.map { category ->
            listOf(
                category.name,
                category.color,
                (category.monthlyBudget ?: 0.0).toString()
            )
        }

        service.spreadsheets().values()
            .append(spreadsheetId, "$CATEGORIES_SHEET!A2", ValueRange().setValues(rows))
            .setValueInputOption("RAW")
            .execute()
    }

    private fun getSheetProperties(service: Sheets, sheetName: String): SheetProperties? {
        val spreadsheet = service.spreadsheets()
            .get(spreadsheetId)
            .setFields("sheets.properties")
            .execute()

        return spreadsheet.sheets
            ?.mapNotNull { it.properties }
            ?.firstOrNull { it.title == sheetName }
    }

    private fun List<Any>.valueAt(index: Int): String = getOrNull(index)?.toString().orEmpty()

    private fun parseDate(value: String): LocalDate {
        return runCatching { LocalDate.parse(value) }.getOrDefault(LocalDate.now())
    }

    private fun parseDateTime(value: String): LocalDateTime {
        return runCatching { LocalDateTime.parse(value) }.getOrDefault(LocalDateTime.now())
    }
}
