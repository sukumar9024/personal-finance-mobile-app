package com.financetracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financetracker.data.model.Category
import com.financetracker.data.model.Expense
import com.financetracker.data.model.IncomeEntry
import com.financetracker.data.model.RecurringEntry
import com.financetracker.data.model.RecurringType
import com.financetracker.data.model.isTransfer
import com.financetracker.data.model.spendingTotal
import com.financetracker.data.repository.GoogleSheetsRepository
import com.financetracker.ui.theme.ThemeMode
import com.financetracker.data.model.Currency
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import java.util.UUID

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

data class SyncStatus(
    val isLiveSyncEnabled: Boolean = false,
    val isUsingCachedData: Boolean = false,
    val lastSyncAttemptMillis: Long? = null,
    val lastSuccessfulSyncMillis: Long? = null,
    val lastSyncError: String? = null
)

data class PendingUndoDelete(
    val expense: Expense,
    val token: Long = System.currentTimeMillis()
)

data class OverspendingAlert(
    val title: String,
    val message: String,
    val token: Long = System.currentTimeMillis()
)

data class FinanceTrackerUiState(
    val expenses: List<Expense> = emptyList(),
    val reportExpenses: List<Expense> = emptyList(),
    val incomeEntries: List<IncomeEntry> = emptyList(),
    val recurringEntries: List<RecurringEntry> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val categoryState: CategoryState = CategoryState(),
    val currentMonthSheet: String = "",
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val syncStatus: SyncStatus = SyncStatus(),
    val pendingUndoDelete: PendingUndoDelete? = null,
    val overspendingAlert: OverspendingAlert? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currency: Currency = Currency.getDefault()
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GoogleSheetsRepository(application)

    private val _uiState = MutableStateFlow(
        FinanceTrackerUiState(
            currentMonthSheet = repository.getCurrentMonthSheetName(),
            syncStatus = buildSyncStatus(isUsingCachedData = false)
        )
    )
    val uiState: StateFlow<FinanceTrackerUiState> = _uiState.asStateFlow()

    init {
        hydrateFromCache()
        refreshAllData()
    }

    fun refreshData() {
        refreshAllData()
    }

    fun loadExpenses() {
        refreshAllData()
    }

    fun loadCategories() {
        refreshAllData()
    }

    fun addExpense(expense: Expense) {
        if (!repository.isReadyForLiveSync()) {
            val newExpenses = _uiState.value.expenses + expense
            updateLocalState(
                _uiState.value.copy(
                    expenses = newExpenses.sortedByDescending { it.date },
                    reportExpenses = (_uiState.value.reportExpenses + expense).sortedByDescending { it.date },
                    totalAmount = newExpenses.spendingTotal(),
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            val currentSheet = currentSheetName()
            runCatching {
                repository.addExpense(currentSheet, expense)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to add expense to Google Sheets.")
            }
        }
    }

    fun addExpenseGroup(expenses: List<Expense>) {
        val normalizedExpenses = expenses.filter { it.amount > 0.0 }
        if (normalizedExpenses.isEmpty()) return

        if (!repository.isReadyForLiveSync()) {
            val newExpenses = (_uiState.value.expenses + normalizedExpenses)
                .sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.modifiedAt })
            val newReportExpenses = (_uiState.value.reportExpenses + normalizedExpenses)
                .sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.modifiedAt })
            updateLocalState(
                _uiState.value.copy(
                    expenses = newExpenses,
                    reportExpenses = newReportExpenses,
                    totalAmount = newExpenses.spendingTotal(),
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            val currentSheet = currentSheetName()
            runCatching {
                normalizedExpenses.forEach { expense ->
                    repository.addExpense(currentSheet, expense)
                }
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to save transaction group.")
            }
        }
    }

    fun updateExpense(expense: Expense) {
        if (!repository.isReadyForLiveSync()) {
            val newExpenses = _uiState.value.expenses.map {
                if (it.id == expense.id) expense else it
            }
            val newReportExpenses = _uiState.value.reportExpenses.map {
                if (it.id == expense.id) expense else it
            }
            updateLocalState(
                _uiState.value.copy(
                    expenses = newExpenses.sortedByDescending { it.date },
                    reportExpenses = newReportExpenses.sortedByDescending { it.date },
                    totalAmount = newExpenses.spendingTotal(),
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            val currentSheet = currentSheetName()
            runCatching {
                repository.updateExpense(currentSheet, expense)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to update expense in Google Sheets.")
            }
        }
    }

    fun deleteExpense(id: String) {
        val deletedExpense = _uiState.value.expenses.find { it.id == id } ?: _uiState.value.reportExpenses.find { it.id == id }
        if (!repository.isReadyForLiveSync()) {
            val newExpenses = _uiState.value.expenses.filter { it.id != id }
            val newReportExpenses = _uiState.value.reportExpenses.filter { it.id != id }
            updateLocalState(
                _uiState.value.copy(
                    expenses = newExpenses,
                    reportExpenses = newReportExpenses,
                    totalAmount = newExpenses.spendingTotal(),
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true),
                    pendingUndoDelete = deletedExpense?.let(::PendingUndoDelete)
                )
            )
            return
        }

        viewModelScope.launch {
            val currentSheet = currentSheetName()
            runCatching {
                repository.deleteExpense(currentSheet, id)
            }.onSuccess {
                refreshAllData(pendingUndoExpense = deletedExpense)
            }.onFailure { error ->
                applyError(error.message ?: "Failed to delete expense from Google Sheets.")
            }
        }
    }

    fun restoreDeletedExpense() {
        val pending = _uiState.value.pendingUndoDelete ?: return
        val clearedState = _uiState.value.copy(pendingUndoDelete = null)
        updateLocalState(clearedState)
        addExpense(
            pending.expense.copy(
                id = UUID.randomUUID().toString(),
                sheetRowIndex = -1
            )
        )
    }

    fun clearPendingUndoDelete() {
        val pending = _uiState.value.pendingUndoDelete ?: return
        updateLocalState(_uiState.value.copy(pendingUndoDelete = null))
    }

    fun consumeOverspendingAlert() {
        if (_uiState.value.overspendingAlert == null) return
        updateLocalState(_uiState.value.copy(overspendingAlert = null))
    }

fun setThemeMode(themeMode: ThemeMode) {
    updateLocalState(_uiState.value.copy(themeMode = themeMode))
}

fun setCurrency(currency: Currency) {
    updateLocalState(_uiState.value.copy(currency = currency))
}

    fun setMonthlyIncome(amount: Double) {
        setMonthlyIncomeForPeriod(periodFromSheet(currentSheetName()), amount)
    }

    fun setMonthlyIncomeForPeriod(period: String, amount: Double) {
        if (!repository.isReadyForLiveSync()) {
            val updatedEntries = _uiState.value.incomeEntries
                .filterNot { it.period == period } + IncomeEntry(period = period, amount = amount)
            updateLocalState(
                _uiState.value.copy(
                    incomeEntries = updatedEntries.sortedByDescending { it.period },
                    monthlyIncome = incomeForPeriod(updatedEntries, currentSheetName()),
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.upsertMonthlyIncome(period, amount)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to save monthly income.")
            }
        }
    }

    fun addRecurringExpense(
        title: String,
        amount: Double,
        dayOfMonth: Int,
        category: String,
        description: String,
        paymentMethod: String
    ) {
        addRecurringEntry(
            RecurringEntry(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { category },
                amount = amount,
                type = RecurringType.EXPENSE,
                dayOfMonth = dayOfMonth,
                category = category,
                description = description,
                paymentMethod = paymentMethod
            )
        )
    }

    fun addRecurringIncome(title: String, amount: Double, dayOfMonth: Int) {
        addRecurringEntry(
            RecurringEntry(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { "Monthly income" },
                amount = amount,
                type = RecurringType.INCOME,
                dayOfMonth = dayOfMonth
            )
        )
    }

    fun toggleRecurringEntry(entry: RecurringEntry, active: Boolean) {
        val updatedEntry = entry.copy(active = active)
        if (!repository.isReadyForLiveSync()) {
            updateLocalState(
                _uiState.value.copy(
                    recurringEntries = _uiState.value.recurringEntries.map {
                        if (it.id == entry.id) updatedEntry else it
                    },
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.updateRecurringEntry(updatedEntry)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to update recurring entry.")
            }
        }
    }

    fun updateCategoryBudget(name: String, monthlyBudget: Double?) {
        if (!repository.isReadyForLiveSync()) {
            val updatedCategories = _uiState.value.categoryState.categories.map {
                if (it.name == name) it.copy(monthlyBudget = monthlyBudget) else it
            }
            updateLocalState(
                _uiState.value.copy(
                    categoryState = _uiState.value.categoryState.copy(categories = updatedCategories),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.updateCategoryBudget(name, monthlyBudget)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to update category budget.")
            }
        }
    }

    fun addCategory(name: String, color: String) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return

        val category = Category(
            name = normalizedName,
            color = color.takeIf { it.isNotBlank() } ?: suggestedColorFor(normalizedName),
            icon = "default",
            monthlyBudget = null
        )

        if (!repository.isReadyForLiveSync()) {
            val merged = mergeWithDefaultCategories(_uiState.value.categoryState.categories + category)
            updateLocalState(
                _uiState.value.copy(
                    categoryState = _uiState.value.categoryState.copy(categories = merged),
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.addCategory(category)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to add category.")
            }
        }
    }

    private fun addRecurringEntry(entry: RecurringEntry) {
        if (!repository.isReadyForLiveSync()) {
            updateLocalState(
                _uiState.value.copy(
                    recurringEntries = (_uiState.value.recurringEntries + entry).sortedBy { it.dayOfMonth },
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.addRecurringEntry(entry)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to save recurring entry.")
            }
        }
    }

    private fun hydrateFromCache() {
        val cachedData = repository.loadCachedData() ?: return
        val currentSheet = repository.getCurrentMonthSheetName()
        val categories = mergeWithDefaultCategories(
            cachedData.categories.ifEmpty { repository.getDefaultCategories() }
        )
        val stateFromCache = _uiState.value.copy(
            currentMonthSheet = currentSheet,
            expenses = cachedData.expenses,
            reportExpenses = cachedData.reportExpenses.ifEmpty { cachedData.expenses },
            incomeEntries = cachedData.incomeEntries.sortedByDescending { it.period },
            recurringEntries = cachedData.recurringEntries.sortedBy { it.dayOfMonth },
            monthlyIncome = incomeForPeriod(cachedData.incomeEntries, currentSheet),
            categoryState = CategoryState(categories = categories, isLoading = false),
            totalAmount = cachedData.expenses.spendingTotal(),
            syncStatus = buildSyncStatus(isUsingCachedData = true),
            errorMessage = null
        )
        _uiState.value = stateFromCache
    }

    private fun refreshAllData(pendingUndoExpense: Expense? = null) {
        viewModelScope.launch {
            val existingState = _uiState.value
            _uiState.value = existingState.copy(
                isLoading = true,
                categoryState = existingState.categoryState.copy(isLoading = true),
                syncStatus = buildSyncStatus(
                    isUsingCachedData = existingState.syncStatus.isUsingCachedData,
                    overrideAttemptMillis = if (repository.isReadyForLiveSync()) System.currentTimeMillis() else existingState.syncStatus.lastSyncAttemptMillis
                )
            )

            val currentSheet = repository.getCurrentMonthSheetName()
            val isLiveSyncReady = repository.isReadyForLiveSync()

            if (!isLiveSyncReady) {
                val categories = mergeWithDefaultCategories(
                    existingState.categoryState.categories.ifEmpty { repository.getDefaultCategories() }
                )
                updateLocalState(
                    existingState.copy(
                        currentMonthSheet = currentSheet,
                        categoryState = CategoryState(categories = categories, isLoading = false),
                        monthlyIncome = incomeForPeriod(existingState.incomeEntries, currentSheet),
                        totalAmount = existingState.expenses.spendingTotal(),
                        isLoading = false,
                        errorMessage = repository.getConfigurationStatusMessage(),
                        syncStatus = buildSyncStatus(isUsingCachedData = true)
                    )
                )
                return@launch
            }

            repository.recordSyncAttempt()

            val refreshResults = coroutineScope {
                val categoriesDeferred = async { runCatching { repository.fetchCategories() } }
                val expensesDeferred = async { runCatching { repository.fetchExpenses(currentSheet) } }
                val reportExpensesDeferred = async { runCatching { repository.fetchAllExpenses() } }
                val incomeEntriesDeferred = async { runCatching { repository.fetchIncomeEntries() } }
                val recurringDeferred = async { runCatching { repository.fetchRecurringEntries() } }

                RefreshResults(
                    categories = categoriesDeferred.await(),
                    expenses = expensesDeferred.await(),
                    reportExpenses = reportExpensesDeferred.await(),
                    incomeEntries = incomeEntriesDeferred.await(),
                    recurringEntries = recurringDeferred.await()
                )
            }

            val categories = mergeWithDefaultCategories(
                refreshResults.categories.getOrDefault(
                    existingState.categoryState.categories.ifEmpty { repository.getDefaultCategories() }
                )
            )
            var expenses = refreshResults.expenses.getOrDefault(existingState.expenses)
            var reportExpenses = refreshResults.reportExpenses.getOrDefault(existingState.reportExpenses.ifEmpty { expenses })
            var incomeEntries = refreshResults.incomeEntries.getOrDefault(existingState.incomeEntries)
            val recurringEntries = refreshResults.recurringEntries.getOrDefault(existingState.recurringEntries)

            runCatching {
                repository.applyRecurringEntries(
                    currentSheet = currentSheet,
                    existingExpenses = expenses,
                    allExpenses = reportExpenses,
                    incomeEntries = incomeEntries,
                    recurringEntries = recurringEntries
                )
            }.onSuccess { applied ->
                expenses = applied.expenses
                reportExpenses = applied.reportExpenses
                incomeEntries = applied.incomeEntries
            }

            val firstLoadError = listOfNotNull(
                refreshResults.categories.exceptionOrNull()?.message,
                refreshResults.expenses.exceptionOrNull()?.message,
                refreshResults.reportExpenses.exceptionOrNull()?.message,
                refreshResults.incomeEntries.exceptionOrNull()?.message,
                refreshResults.recurringEntries.exceptionOrNull()?.message,
                repository.getConfigurationStatusMessage()
            ).firstOrNull()

            if (firstLoadError == null) {
                repository.recordSyncSuccess()
            } else {
                repository.recordSyncFailure(firstLoadError)
            }

                val updatedState = existingState.copy(
                    currentMonthSheet = currentSheet,
                    categoryState = CategoryState(categories = categories, isLoading = false),
                    expenses = expenses.sortedByDescending { it.date },
                    reportExpenses = reportExpenses.sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.modifiedAt }),
                    incomeEntries = incomeEntries.sortedByDescending { it.period },
                    recurringEntries = recurringEntries.sortedBy { it.dayOfMonth },
                    monthlyIncome = incomeForPeriod(incomeEntries, currentSheet),
                    totalAmount = expenses.spendingTotal(),
                    isLoading = false,
                    errorMessage = firstLoadError,
                    syncStatus = buildSyncStatus(isUsingCachedData = firstLoadError != null),
                    pendingUndoDelete = pendingUndoExpense?.let(::PendingUndoDelete),
                    overspendingAlert = buildOverspendingAlert(
                        categories = categories,
                        monthlyIncome = incomeForPeriod(incomeEntries, currentSheet),
                        totalAmount = expenses.spendingTotal(),
                        expenses = expenses
                    ),
                    themeMode = existingState.themeMode
                )

            updateLocalState(updatedState)
        }
    }

    private fun updateLocalState(newState: FinanceTrackerUiState) {
        _uiState.value = newState
        repository.saveCachedData(
            GoogleSheetsRepository.CachedFinanceData(
                currentMonthSheet = newState.currentMonthSheet,
                expenses = newState.expenses,
                reportExpenses = newState.reportExpenses,
                categories = newState.categoryState.categories,
                incomeEntries = newState.incomeEntries,
                recurringEntries = newState.recurringEntries
            )
        )
    }

    private fun applyError(message: String) {
        repository.recordSyncFailure(message)
        updateLocalState(
            _uiState.value.copy(
                isLoading = false,
                categoryState = _uiState.value.categoryState.copy(isLoading = false),
                errorMessage = message,
                syncStatus = buildSyncStatus(isUsingCachedData = true),
                overspendingAlert = buildOverspendingAlert(
                    categories = _uiState.value.categoryState.categories,
                    monthlyIncome = _uiState.value.monthlyIncome,
                    totalAmount = _uiState.value.totalAmount,
                    expenses = _uiState.value.expenses
                )
            )
        )
    }

    private fun buildSyncStatus(
        isUsingCachedData: Boolean,
        overrideAttemptMillis: Long? = null
    ): SyncStatus {
        val snapshot = repository.getSyncSnapshot()
        return SyncStatus(
            isLiveSyncEnabled = repository.isReadyForLiveSync(),
            isUsingCachedData = isUsingCachedData,
            lastSyncAttemptMillis = overrideAttemptMillis ?: snapshot.lastSyncAttemptMillis,
            lastSuccessfulSyncMillis = snapshot.lastSuccessfulSyncMillis,
            lastSyncError = snapshot.lastSyncError
        )
    }

    private fun incomeForPeriod(entries: List<IncomeEntry>, sheetName: String): Double {
        val period = periodFromSheet(sheetName)
        return entries.firstOrNull { it.period == period }?.amount ?: 0.0
    }

    private fun currentSheetName(): String {
        return _uiState.value.currentMonthSheet.ifBlank { repository.getCurrentMonthSheetName() }
    }

    private fun defaultCategories(): List<Category> {
        return repository.getDefaultCategories()
    }

    private fun mergeWithDefaultCategories(categories: List<Category>): List<Category> {
        val categoryMap = linkedMapOf<String, Category>()

        defaultCategories().forEach { category ->
            categoryMap[category.name.lowercase(Locale.getDefault())] = category
        }

        categories.forEach { category ->
            categoryMap[category.name.lowercase(Locale.getDefault())] = category
        }

        return categoryMap.values.toList()
    }

    private fun suggestedColorFor(name: String): String {
        val palette = listOf(
            "#FF5722",
            "#2196F3",
            "#E91E63",
            "#9C27B0",
            "#FF9800",
            "#4CAF50",
            "#3F51B5",
            "#10B981",
            "#F97316",
            "#607D8B"
        )

        return palette[name.hashCode().mod(palette.size)]
    }

    private fun periodFromSheet(sheetName: String): String {
        val rawPeriod = sheetName.removePrefix("expenses_").replace("_", "-")
        return runCatching { YearMonth.parse(rawPeriod).toString() }.getOrDefault(rawPeriod)
    }

    private fun buildOverspendingAlert(
        categories: List<Category>,
        monthlyIncome: Double,
        totalAmount: Double,
        expenses: List<Expense>
    ): OverspendingAlert? {
        if (monthlyIncome > 0.0 && totalAmount > monthlyIncome) {
            return OverspendingAlert(
                title = "Monthly budget exceeded",
                message = "Spending has reached ${totalAmount.toDisplayAmount()} against a budget of ${monthlyIncome.toDisplayAmount()}."
            )
        }

        val spendByCategory = expenses
            .filterNot { it.isTransfer }
            .groupBy { it.category }
            .mapValues { (_, entries) -> entries.sumOf { it.amount } }
        val overspentCategory = categories.firstOrNull { category ->
            val budget = category.monthlyBudget ?: 0.0
            budget > 0.0 && (spendByCategory[category.name] ?: 0.0) > budget
        }

        return overspentCategory?.let { category ->
            val spent = spendByCategory[category.name] ?: 0.0
            OverspendingAlert(
                title = "${category.name} budget exceeded",
                message = "${category.name} spending is ${spent.toDisplayAmount()} against ${category.monthlyBudget?.toDisplayAmount().orEmpty()}."
            )
        }
    }

    private fun Double.toDisplayAmount(): String {
        val integerValue = toLong()
        return if (this == integerValue.toDouble()) integerValue.toString() else String.format(Locale.US, "%.2f", this)
    }

    fun buildSplitExpenses(
        date: LocalDate,
        paymentMethod: String,
        description: String,
        tags: List<String>,
        splitRows: List<SplitExpenseInput>
    ): List<Expense> {
        val splitGroupId = UUID.randomUUID().toString()
        return splitRows.mapNotNull { row ->
            val amount = row.amount.takeIf { it > 0.0 } ?: return@mapNotNull null
            Expense(
                id = UUID.randomUUID().toString(),
                date = date,
                amount = amount,
                category = row.category,
                subcategory = row.subcategory?.takeIf { it.isNotBlank() },
                description = description,
                paymentMethod = paymentMethod,
                splitGroupId = splitGroupId,
                tags = tags
            )
        }
    }

    private data class RefreshResults(
        val categories: Result<List<Category>>,
        val expenses: Result<List<Expense>>,
        val reportExpenses: Result<List<Expense>>,
        val incomeEntries: Result<List<IncomeEntry>>,
        val recurringEntries: Result<List<RecurringEntry>>
    )
}

data class SplitExpenseInput(
    val category: String,
    val amount: Double,
    val subcategory: String? = null
)
