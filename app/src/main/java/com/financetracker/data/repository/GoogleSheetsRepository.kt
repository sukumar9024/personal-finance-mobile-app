package com.financetracker.data.repository

import android.content.Context
import com.financetracker.BuildConfig
import com.financetracker.data.model.Category
import com.financetracker.data.model.Expense
import com.financetracker.data.model.IncomeEntry
import com.financetracker.data.model.RecurringEntry
import com.financetracker.data.model.RecurringType
import com.financetracker.data.model.TransactionType
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class GoogleSheetsRepository(private val context: Context) {
    companion object {
        private const val CATEGORIES_SHEET = "categories"
        private const val INCOME_SHEET = "monthly_income"
        private const val RECURRING_SHEET = "recurring_entries"
        private const val SERVICE_ACCOUNT_ASSET = "service-account-key.json"
        private const val CACHE_PREFS = "finance_tracker_cache"
        private const val CACHE_KEY = "cached_finance_data"
        private const val LAST_SYNC_ATTEMPT_KEY = "last_sync_attempt"
        private const val LAST_SUCCESSFUL_SYNC_KEY = "last_successful_sync"
        private const val LAST_SYNC_ERROR_KEY = "last_sync_error"
        private val EXPENSE_SHEET_REGEX = Regex("""expenses_\d{4}_\d{2}""")

        private val DEFAULT_CATEGORIES = listOf(
            Category("Food", "#FF5722", "restaurant", 8000.0),
            Category("Transport", "#2196F3", "directions_car", 4000.0),
            Category("Shopping", "#E91E63", "shopping_bag", 6000.0),
            Category("Bills", "#9C27B0", "receipt", 5000.0),
            Category("Entertainment", "#FF9800", "movie", 3000.0),
            Category("Health", "#4CAF50", "local_hospital", 2500.0),
            Category("Education", "#3F51B5", "school", 3500.0),
            Category("Investment", "#10B981", "savings", 7000.0),
            Category("Family Support", "#F97316", "favorite", 5000.0),
            Category("Other", "#607D8B", "more_horiz", 2000.0)
        )

        private val EXPENSE_HEADERS = listOf(
            "Date",
            "Amount",
            "Category",
            "Subcategory",
            "Description",
            "Payment Method",
            "Transfer Account",
            "Transfer Destination Account",
            "Transaction Type",
            "Split Group ID",
            "Receipt URL",
            "Tags",
            "Created At",
            "Modified At",
            "Recurring ID",
            "Occurrence Period"
        )

        private val CATEGORY_HEADERS = listOf("Name", "Color", "Monthly Budget")
        private val INCOME_HEADERS = listOf("Month", "Income", "Recurring ID")
        private val RECURRING_HEADERS = listOf(
            "ID",
            "Title",
            "Amount",
            "Type",
            "Day Of Month",
            "Category",
            "Description",
            "Payment Method",
            "Active"
        )
    }

    data class CachedFinanceData(
        val currentMonthSheet: String,
        val expenses: List<Expense>,
        val reportExpenses: List<Expense>,
        val categories: List<Category>,
        val incomeEntries: List<IncomeEntry>,
        val recurringEntries: List<RecurringEntry>
    )

    data class SyncSnapshot(
        val lastSyncAttemptMillis: Long? = null,
        val lastSuccessfulSyncMillis: Long? = null,
        val lastSyncError: String? = null
    )

    data class AppliedRecurringData(
        val expenses: List<Expense>,
        val reportExpenses: List<Expense>,
        val incomeEntries: List<IncomeEntry>
    )

    private val prefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
    private val transport = NetHttpTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val spreadsheetId = BuildConfig.SPREADSHEET_ID.trim()

    @Volatile
    private var sheetsService: Sheets? = null

    @Volatile
    private var sheetPropertiesCache: Map<String, SheetProperties>? = null

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

    fun getDefaultCategories(): List<Category> = DEFAULT_CATEGORIES

    fun loadCachedData(): CachedFinanceData? {
        val rawJson = prefs.getString(CACHE_KEY, null) ?: return null
        return runCatching {
            val json = JSONObject(rawJson)
            CachedFinanceData(
                currentMonthSheet = json.optString("currentMonthSheet", getCurrentMonthSheetName()),
                expenses = json.optJSONArray("expenses").toExpenseList(),
                reportExpenses = json.optJSONArray("reportExpenses").toExpenseList(),
                categories = json.optJSONArray("categories").toCategoryList(),
                incomeEntries = json.optJSONArray("incomeEntries").toIncomeEntryList(),
                recurringEntries = json.optJSONArray("recurringEntries").toRecurringEntryList()
            )
        }.getOrNull()
    }

    fun saveCachedData(data: CachedFinanceData) {
        val json = JSONObject().apply {
            put("currentMonthSheet", data.currentMonthSheet)
            put("expenses", JSONArray().apply { data.expenses.forEach { put(it.toJson()) } })
            put("reportExpenses", JSONArray().apply { data.reportExpenses.forEach { put(it.toJson()) } })
            put("categories", JSONArray().apply { data.categories.forEach { put(it.toJson()) } })
            put("incomeEntries", JSONArray().apply { data.incomeEntries.forEach { put(it.toJson()) } })
            put("recurringEntries", JSONArray().apply { data.recurringEntries.forEach { put(it.toJson()) } })
        }

        prefs.edit().putString(CACHE_KEY, json.toString()).apply()
    }

    fun getSyncSnapshot(): SyncSnapshot {
        return SyncSnapshot(
            lastSyncAttemptMillis = prefs.getLong(LAST_SYNC_ATTEMPT_KEY, 0L).takeIf { it > 0L },
            lastSuccessfulSyncMillis = prefs.getLong(LAST_SUCCESSFUL_SYNC_KEY, 0L).takeIf { it > 0L },
            lastSyncError = prefs.getString(LAST_SYNC_ERROR_KEY, null)
        )
    }

    fun recordSyncAttempt() {
        prefs.edit().putLong(LAST_SYNC_ATTEMPT_KEY, System.currentTimeMillis()).apply()
    }

    fun recordSyncSuccess() {
        prefs.edit()
            .putLong(LAST_SUCCESSFUL_SYNC_KEY, System.currentTimeMillis())
            .remove(LAST_SYNC_ERROR_KEY)
            .apply()
    }

    fun recordSyncFailure(message: String) {
        prefs.edit().putString(LAST_SYNC_ERROR_KEY, message).apply()
    }

    suspend fun fetchExpenses(sheetName: String): List<Expense> = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext emptyList()
        ensureExpenseSheet(service, sheetName)
        readExpensesFromSheet(service, sheetName)
    }

    suspend fun addExpense(sheetName: String, expense: Expense): Boolean = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext false
        ensureExpenseSheet(service, sheetName)
        appendExpenseRow(service, sheetName, expense)
        true
    }

    suspend fun updateExpense(sheetName: String, expense: Expense): Boolean = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext false
        if (expense.sheetRowIndex <= 1) return@withContext false

        service.spreadsheets().values()
            .update(
                spreadsheetId,
                "$sheetName!A${expense.sheetRowIndex}:O${expense.sheetRowIndex}",
                ValueRange().setValues(listOf(expense.toSheetRow()))
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

    suspend fun addCategory(category: Category): Boolean = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext false
        ensureCategoriesSheet(service)

        service.spreadsheets().values()
            .append(
                spreadsheetId,
                "$CATEGORIES_SHEET!A:C",
                ValueRange().setValues(
                    listOf(
                        listOf(
                            category.name,
                            category.color,
                            category.monthlyBudget?.toString().orEmpty()
                        )
                    )
                )
            )
            .setValueInputOption("RAW")
            .execute()

        true
    }

    suspend fun updateCategoryBudget(categoryName: String, monthlyBudget: Double?): Boolean = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext false
        ensureCategoriesSheet(service)

        val rows = service.spreadsheets().values()
            .get(spreadsheetId, "$CATEGORIES_SHEET!A2:C")
            .execute()
            .getValues()
            .orEmpty()

        val index = rows.indexOfFirst { row -> row.valueAt(0).equals(categoryName, ignoreCase = true) }
        if (index == -1) return@withContext false

        val rowNumber = index + 2
        val existing = rows[index]
        service.spreadsheets().values()
            .update(
                spreadsheetId,
                "$CATEGORIES_SHEET!A$rowNumber:C$rowNumber",
                ValueRange().setValues(
                    listOf(
                        listOf(
                            existing.valueAt(0),
                            existing.valueAt(1),
                            monthlyBudget?.toString().orEmpty()
                        )
                    )
                )
            )
            .setValueInputOption("RAW")
            .execute()

        true
    }

    suspend fun fetchAllExpenses(): List<Expense> = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext emptyList()

        getExpenseSheetTitles(service)
            .flatMap { sheetName ->
                readExpensesFromSheet(service, sheetName, includeSheetPrefix = true)
            }
            .sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.modifiedAt })
    }

    suspend fun fetchIncomeEntries(): List<IncomeEntry> = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext emptyList()
        fetchIncomeEntries(service)
    }

    suspend fun upsertMonthlyIncome(
        period: String,
        amount: Double,
        recurringEntryId: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext false
        upsertMonthlyIncome(service, period, amount, recurringEntryId)
    }

    suspend fun fetchRecurringEntries(): List<RecurringEntry> = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext emptyList()
        ensureRecurringSheet(service)

        val rows = service.spreadsheets().values()
            .get(spreadsheetId, "$RECURRING_SHEET!A2:I")
            .execute()
            .getValues()
            .orEmpty()

        rows.mapIndexedNotNull { index, row ->
            val type = runCatching { RecurringType.valueOf(row.valueAt(3).ifBlank { "EXPENSE" }) }.getOrNull()
                ?: return@mapIndexedNotNull null
            RecurringEntry(
                id = row.valueAt(0).ifBlank { "recurring_${index + 2}" },
                title = row.valueAt(1).ifBlank { "Recurring ${type.name.lowercase().replaceFirstChar { it.uppercase() }}" },
                amount = row.valueAt(2).toDoubleOrNull() ?: return@mapIndexedNotNull null,
                type = type,
                dayOfMonth = row.valueAt(4).toIntOrNull()?.coerceIn(1, 31) ?: 1,
                category = row.valueAt(5).ifBlank { null },
                description = row.valueAt(6),
                paymentMethod = row.valueAt(7).ifBlank { "Cash" },
                active = row.valueAt(8).ifBlank { "true" }.toBooleanStrictOrNull() ?: true,
                sheetRowIndex = index + 2
            )
        }
    }

    suspend fun addRecurringEntry(entry: RecurringEntry): Boolean = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext false
        ensureRecurringSheet(service)

        service.spreadsheets().values()
            .append(spreadsheetId, "$RECURRING_SHEET!A:I", ValueRange().setValues(listOf(entry.toSheetRow())))
            .setValueInputOption("RAW")
            .execute()

        true
    }

    suspend fun updateRecurringEntry(entry: RecurringEntry): Boolean = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext false
        val rowIndex = entry.sheetRowIndex.takeIf { it > 1 } ?: findRecurringRowIndex(service, entry.id)
        if (rowIndex <= 1) return@withContext false

        service.spreadsheets().values()
            .update(
                spreadsheetId,
                "$RECURRING_SHEET!A$rowIndex:I$rowIndex",
                ValueRange().setValues(listOf(entry.toSheetRow()))
            )
            .setValueInputOption("RAW")
            .execute()

        true
    }

    suspend fun applyRecurringEntries(
        currentSheet: String,
        existingExpenses: List<Expense>,
        allExpenses: List<Expense>,
        incomeEntries: List<IncomeEntry>,
        recurringEntries: List<RecurringEntry>
    ): AppliedRecurringData = withContext(Dispatchers.IO) {
        val service = getSheetsService() ?: return@withContext AppliedRecurringData(existingExpenses, allExpenses, incomeEntries)
        if (recurringEntries.none { it.active }) {
            return@withContext AppliedRecurringData(existingExpenses, allExpenses, incomeEntries)
        }

        val period = periodFromSheet(currentSheet)
        val currentMonth = runCatching { YearMonth.parse(period) }.getOrDefault(YearMonth.now())
        val cutoffDay = if (currentMonth == YearMonth.now()) {
            LocalDate.now().dayOfMonth
        } else {
            currentMonth.lengthOfMonth()
        }

        var createdExpense = false
        var createdIncome = false

        recurringEntries
            .filter { it.active && it.dayOfMonth <= cutoffDay }
            .forEach { entry ->
                when (entry.type) {
                    RecurringType.EXPENSE -> {
                        val exists = allExpenses.any { expense ->
                            expense.recurringEntryId == entry.id && expense.occurrencePeriod == period
                        }
                        if (!exists) {
                            appendExpenseRow(
                                service = service,
                                sheetName = currentSheet,
                                expense = Expense(
                                    date = currentMonth.atDay(entry.dayOfMonth.coerceAtMost(currentMonth.lengthOfMonth())),
                                    amount = entry.amount,
                                    category = entry.category ?: "Other",
                                    description = entry.description.ifBlank { entry.title },
                                    paymentMethod = entry.paymentMethod,
                                    recurringEntryId = entry.id,
                                    occurrencePeriod = period
                                )
                            )
                            createdExpense = true
                        }
                    }

                    RecurringType.INCOME -> {
                        val exists = incomeEntries.any { income -> income.period == period }
                        if (!exists) {
                            upsertMonthlyIncome(service, period, entry.amount, entry.id)
                            createdIncome = true
                        }
                    }
                }
            }

        if (!createdExpense && !createdIncome) {
            return@withContext AppliedRecurringData(existingExpenses, allExpenses, incomeEntries)
        }

        val refreshedExpenses = readExpensesFromSheet(service, currentSheet)
        val refreshedReportExpenses = getExpenseSheetTitles(service)
            .flatMap { sheetName -> readExpensesFromSheet(service, sheetName, includeSheetPrefix = true) }
            .sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.modifiedAt })
        val refreshedIncomeEntries = fetchIncomeEntries(service)

        AppliedRecurringData(
            expenses = refreshedExpenses,
            reportExpenses = refreshedReportExpenses,
            incomeEntries = refreshedIncomeEntries
        )
    }

    suspend fun fetchMonthlySummary(sheetName: String): Map<String, Double> = withContext(Dispatchers.IO) {
        fetchExpenses(sheetName)
            .groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }

    suspend fun syncData(): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentSheet = getCurrentMonthSheetName()
            
            // Fetch all data from sheets
            val expenses = fetchExpenses(currentSheet)
            val allExpenses = fetchAllExpenses()
            val categories = fetchCategories()
            val incomeEntries = fetchIncomeEntries()
            val recurringEntries = fetchRecurringEntries()
            
            // Apply recurring entries
            val appliedData = applyRecurringEntries(
                currentSheet = currentSheet,
                existingExpenses = expenses,
                allExpenses = allExpenses,
                incomeEntries = incomeEntries,
                recurringEntries = recurringEntries
            )
            
            // Save to cache
            saveCachedData(
                CachedFinanceData(
                    currentMonthSheet = currentSheet,
                    expenses = appliedData.expenses,
                    reportExpenses = appliedData.reportExpenses,
                    categories = categories,
                    incomeEntries = appliedData.incomeEntries,
                    recurringEntries = recurringEntries
                )
            )
            
            recordSyncSuccess()
            true
        } catch (e: Exception) {
            recordSyncFailure(e.message ?: "Unknown sync error")
            false
        }
    }

    private fun getSheetsService(): Sheets? {
        if (spreadsheetId.isBlank()) return null
        sheetsService?.let { return it }

        synchronized(this) {
            sheetsService?.let { return it }
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
                    .also { sheetsService = it }
            }
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

    private fun ensureIncomeSheet(service: Sheets): SheetProperties {
        return ensureSheetExists(service, INCOME_SHEET, INCOME_HEADERS)
    }

    private fun ensureRecurringSheet(service: Sheets): SheetProperties {
        return ensureSheetExists(service, RECURRING_SHEET, RECURRING_HEADERS)
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
        invalidateSheetCache()

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
                category.monthlyBudget?.toString().orEmpty()
            )
        }

        service.spreadsheets().values()
            .append(spreadsheetId, "$CATEGORIES_SHEET!A2:C", ValueRange().setValues(rows))
            .setValueInputOption("RAW")
            .execute()
    }

    private fun getSheetProperties(service: Sheets, sheetName: String): SheetProperties? {
        return getSheetPropertiesMap(service)[sheetName]
    }

    private fun getExpenseSheetTitles(service: Sheets): List<String> {
        return getSheetPropertiesMap(service)
            .keys
            .filter { EXPENSE_SHEET_REGEX.matches(it) }
            .sortedByDescending { it }
    }

    private fun getSheetPropertiesMap(service: Sheets): Map<String, SheetProperties> {
        sheetPropertiesCache?.let { return it }

        val properties = service.spreadsheets()
            .get(spreadsheetId)
            .setFields("sheets.properties")
            .execute()
            .sheets
            ?.mapNotNull { it.properties }
            ?.associateBy { it.title }
            .orEmpty()

        sheetPropertiesCache = properties
        return properties
    }

    private fun invalidateSheetCache() {
        sheetPropertiesCache = null
    }

    private fun fetchIncomeEntries(service: Sheets): List<IncomeEntry> {
        ensureIncomeSheet(service)

        val rows = service.spreadsheets().values()
            .get(spreadsheetId, "$INCOME_SHEET!A2:C")
            .execute()
            .getValues()
            .orEmpty()

        return rows.mapIndexedNotNull { index, row ->
            val period = row.valueAt(0)
            val amount = row.valueAt(1).toDoubleOrNull()

            if (period.isBlank() || amount == null) {
                null
            } else {
                IncomeEntry(
                    period = normalizePeriod(period),
                    amount = amount,
                    recurringEntryId = row.valueAt(2).ifBlank { null },
                    sheetRowIndex = index + 2
                )
            }
        }.sortedByDescending { it.period }
    }

    private fun upsertMonthlyIncome(
        service: Sheets,
        period: String,
        amount: Double,
        recurringEntryId: String?
    ): Boolean {
        ensureIncomeSheet(service)

        val normalizedPeriod = normalizePeriod(period)
        val existingEntries = fetchIncomeEntries(service)
        val existingEntry = existingEntries.firstOrNull { it.period == normalizedPeriod }
        val row = listOf(normalizedPeriod, amount.toString(), recurringEntryId.orEmpty())

        if (existingEntry != null && existingEntry.sheetRowIndex > 1) {
            service.spreadsheets().values()
                .update(
                    spreadsheetId,
                    "$INCOME_SHEET!A${existingEntry.sheetRowIndex}:C${existingEntry.sheetRowIndex}",
                    ValueRange().setValues(listOf(row))
                )
                .setValueInputOption("RAW")
                .execute()
        } else {
            service.spreadsheets().values()
                .append(spreadsheetId, "$INCOME_SHEET!A:C", ValueRange().setValues(listOf(row)))
                .setValueInputOption("RAW")
                .execute()
        }

        return true
    }

    private fun findRecurringRowIndex(service: Sheets, recurringId: String): Int {
        ensureRecurringSheet(service)
        val rows = service.spreadsheets().values()
            .get(spreadsheetId, "$RECURRING_SHEET!A2:A")
            .execute()
            .getValues()
            .orEmpty()

        val index = rows.indexOfFirst { it.valueAt(0) == recurringId }
        return if (index >= 0) index + 2 else -1
    }

    private fun appendExpenseRow(service: Sheets, sheetName: String, expense: Expense) {
        service.spreadsheets().values()
            .append(spreadsheetId, "$sheetName!A:O", ValueRange().setValues(listOf(expense.toSheetRow())))
            .setValueInputOption("RAW")
            .execute()
    }

    private fun readExpensesFromSheet(
        service: Sheets,
        sheetName: String,
        includeSheetPrefix: Boolean = false
    ): List<Expense> {
        val values = service.spreadsheets().values()
            .get(spreadsheetId, "$sheetName!A2:O")
            .execute()
            .getValues()
            .orEmpty()

        return values.mapIndexed { index, row ->
            val rowIndex = index + 2
            val hasExtendedColumns = row.size >= 16
            Expense(
                id = if (includeSheetPrefix) "$sheetName#row_$rowIndex" else "row_$rowIndex",
                date = parseDate(row.valueAt(0)),
                amount = row.valueAt(1).toDoubleOrNull() ?: 0.0,
                category = row.valueAt(2),
                subcategory = row.valueAt(3).ifBlank { null },
                description = row.valueAt(4),
                paymentMethod = row.valueAt(5).ifBlank { "Cash" },
                transferAccount = if (hasExtendedColumns) row.valueAt(6).ifBlank { null } else null,
                transferDestinationAccount = if (hasExtendedColumns) row.valueAt(7).ifBlank { null } else null,
                transactionType = if (hasExtendedColumns) parseTransactionType(row.valueAt(8)) else TransactionType.EXPENSE,
                splitGroupId = if (hasExtendedColumns) row.valueAt(9).ifBlank { null } else null,
                receiptUrl = row.valueAt(if (hasExtendedColumns) 10 else 6).ifBlank { null },
                tags = row.valueAt(if (hasExtendedColumns) 11 else 7)
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() },
                createdAt = parseDateTime(row.valueAt(if (hasExtendedColumns) 12 else 8)),
                modifiedAt = parseDateTime(row.valueAt(if (hasExtendedColumns) 13 else 9)),
                recurringEntryId = row.valueAt(if (hasExtendedColumns) 14 else 10).ifBlank { null },
                occurrencePeriod = row.valueAt(if (hasExtendedColumns) 15 else 11).ifBlank { null },
                sheetRowIndex = rowIndex
            )
        }
    }

    private fun Expense.toSheetRow(): List<String> {
        return listOf(
            date.toString(),
            amount.toString(),
            category,
            subcategory.orEmpty(),
            description,
            paymentMethod,
            transferAccount.orEmpty(),
            transactionType.name,
            splitGroupId.orEmpty(),
            receiptUrl.orEmpty(),
            tags.joinToString(","),
            createdAt.toString(),
            LocalDateTime.now().toString(),
            recurringEntryId.orEmpty(),
            occurrencePeriod.orEmpty()
        )
    }

    private fun RecurringEntry.toSheetRow(): List<String> {
        return listOf(
            id,
            title,
            amount.toString(),
            type.name,
            dayOfMonth.toString(),
            category.orEmpty(),
            description,
            paymentMethod,
            active.toString()
        )
    }

    private fun Expense.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("date", date.toString())
            put("amount", amount)
            put("category", category)
            put("subcategory", subcategory)
            put("description", description)
            put("paymentMethod", paymentMethod)
            put("transferAccount", transferAccount)
            put("transactionType", transactionType.name)
            put("splitGroupId", splitGroupId)
            put("receiptUrl", receiptUrl)
            put("tags", JSONArray(tags))
            put("createdAt", createdAt.toString())
            put("modifiedAt", modifiedAt.toString())
            put("recurringEntryId", recurringEntryId)
            put("occurrencePeriod", occurrencePeriod)
            put("sheetRowIndex", sheetRowIndex)
        }
    }

    private fun Category.toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("color", color)
            put("icon", icon)
            put("monthlyBudget", monthlyBudget)
        }
    }

    private fun IncomeEntry.toJson(): JSONObject {
        return JSONObject().apply {
            put("period", period)
            put("amount", amount)
            put("recurringEntryId", recurringEntryId)
            put("sheetRowIndex", sheetRowIndex)
        }
    }

    private fun RecurringEntry.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("amount", amount)
            put("type", type.name)
            put("dayOfMonth", dayOfMonth)
            put("category", category)
            put("description", description)
            put("paymentMethod", paymentMethod)
            put("active", active)
            put("sheetRowIndex", sheetRowIndex)
        }
    }

    private fun JSONArray?.toExpenseList(): List<Expense> {
        if (this == null) return emptyList()
        return List(length()) { index -> getJSONObject(index).toExpense() }
    }

    private fun JSONArray?.toCategoryList(): List<Category> {
        if (this == null) return emptyList()
        return List(length()) { index -> getJSONObject(index).toCategory() }
    }

    private fun JSONArray?.toIncomeEntryList(): List<IncomeEntry> {
        if (this == null) return emptyList()
        return List(length()) { index -> getJSONObject(index).toIncomeEntry() }
    }

    private fun JSONArray?.toRecurringEntryList(): List<RecurringEntry> {
        if (this == null) return emptyList()
        return List(length()) { index -> getJSONObject(index).toRecurringEntry() }
    }

    private fun JSONObject.toExpense(): Expense {
        return Expense(
            id = optString("id"),
            date = parseDate(optString("date")),
            amount = optDouble("amount"),
            category = optString("category"),
            subcategory = optString("subcategory").ifBlank { null },
            description = optString("description"),
            paymentMethod = optString("paymentMethod", "Cash"),
            transferAccount = optString("transferAccount").ifBlank { null },
            transactionType = parseTransactionType(optString("transactionType", TransactionType.EXPENSE.name)),
            splitGroupId = optString("splitGroupId").ifBlank { null },
            receiptUrl = optString("receiptUrl").ifBlank { null },
            tags = optJSONArray("tags")?.let { tagsArray ->
                List(tagsArray.length()) { index -> tagsArray.optString(index) }
            }.orEmpty(),
            createdAt = parseDateTime(optString("createdAt")),
            modifiedAt = parseDateTime(optString("modifiedAt")),
            recurringEntryId = optString("recurringEntryId").ifBlank { null },
            occurrencePeriod = optString("occurrencePeriod").ifBlank { null },
            sheetRowIndex = optInt("sheetRowIndex", -1)
        )
    }

    private fun JSONObject.toCategory(): Category {
        return Category(
            name = optString("name"),
            color = optString("color"),
            icon = optString("icon", "default"),
            monthlyBudget = optString("monthlyBudget").toDoubleOrNull()
        )
    }

    private fun JSONObject.toIncomeEntry(): IncomeEntry {
        return IncomeEntry(
            period = optString("period"),
            amount = optDouble("amount"),
            recurringEntryId = optString("recurringEntryId").ifBlank { null },
            sheetRowIndex = optInt("sheetRowIndex", -1)
        )
    }

    private fun JSONObject.toRecurringEntry(): RecurringEntry {
        return RecurringEntry(
            id = optString("id"),
            title = optString("title"),
            amount = optDouble("amount"),
            type = runCatching { RecurringType.valueOf(optString("type", RecurringType.EXPENSE.name)) }
                .getOrDefault(RecurringType.EXPENSE),
            dayOfMonth = optInt("dayOfMonth", 1),
            category = optString("category").ifBlank { null },
            description = optString("description"),
            paymentMethod = optString("paymentMethod", "Cash"),
            active = optBoolean("active", true),
            sheetRowIndex = optInt("sheetRowIndex", -1)
        )
    }

    private fun List<Any>.valueAt(index: Int): String = getOrNull(index)?.toString().orEmpty()

    private fun normalizePeriod(period: String): String {
        return runCatching { YearMonth.parse(period.trim()).toString() }.getOrDefault(period.trim())
    }

    private fun parseDate(value: String): LocalDate {
        return runCatching { LocalDate.parse(value) }.getOrDefault(LocalDate.now())
    }

    private fun parseDateTime(value: String): LocalDateTime {
        return runCatching { LocalDateTime.parse(value) }.getOrDefault(LocalDateTime.now())
    }

    private fun parseTransactionType(value: String): TransactionType {
        return runCatching { TransactionType.valueOf(value.ifBlank { TransactionType.EXPENSE.name }) }
            .getOrDefault(TransactionType.EXPENSE)
    }

    private fun periodFromSheet(sheetName: String): String {
        return sheetName.removePrefix("expenses_").replace("_", "-")
    }
}
