package com.financetracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financetracker.data.model.AccountBalance
import com.financetracker.data.model.Category
import com.financetracker.data.model.CategoryBudget
import com.financetracker.data.model.CategoryRolloverSetting
import com.financetracker.data.model.CsvImportMapping
import com.financetracker.data.model.Expense
import com.financetracker.data.model.DashboardCardPreference
import com.financetracker.data.model.DebtAccount
import com.financetracker.data.model.IncomeEntry
import com.financetracker.data.model.InvestmentHolding
import com.financetracker.data.model.MonthlyCloseNote
import com.financetracker.data.model.RecurringEntry
import com.financetracker.data.model.RecurringReminderOccurrence
import com.financetracker.data.model.RecurringReminderStatus
import com.financetracker.data.model.RecurringType
import com.financetracker.data.model.SavingsGoal
import com.financetracker.data.model.TransactionTemplate
import com.financetracker.data.model.isTransfer
import com.financetracker.data.model.spendingTotal
import com.financetracker.data.repository.GoogleSheetsRepository
import com.financetracker.ui.theme.setPreferredCurrency
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
    val sheetName: String,
    val token: Long = System.currentTimeMillis()
)

data class OverspendingAlert(
    val title: String,
    val message: String,
    val token: Long = System.currentTimeMillis()
)

data class ValidationIssue(
    val title: String,
    val detail: String,
    val severity: String
)

data class SetupCheck(
    val title: String,
    val detail: String,
    val passed: Boolean
)

data class FinanceTrackerUiState(
    val expenses: List<Expense> = emptyList(),
    val reportExpenses: List<Expense> = emptyList(),
    val incomeEntries: List<IncomeEntry> = emptyList(),
    val recurringEntries: List<RecurringEntry> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val categoryState: CategoryState = CategoryState(),
    val categoryBudgets: List<CategoryBudget> = emptyList(),
    val currentMonthSheet: String = "",
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val syncStatus: SyncStatus = SyncStatus(),
    val pendingUndoDelete: PendingUndoDelete? = null,
    val overspendingAlert: OverspendingAlert? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currency: Currency = Currency.getDefault(),
    val includeTransfersInReports: Boolean = false,
    val accountBalances: List<AccountBalance> = emptyList(),
    val transactionTemplates: List<TransactionTemplate> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val exchangeRates: Map<String, Double> = emptyMap(),
    val exchangeConversionEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false,
    val dashboardCardPreferences: List<DashboardCardPreference> = emptyList(),
    val categoryRolloverSettings: List<CategoryRolloverSetting> = emptyList(),
    val monthlyCloseNotes: List<MonthlyCloseNote> = emptyList(),
    val csvImportMapping: CsvImportMapping = CsvImportMapping(name = "Default"),
    val debtAccounts: List<DebtAccount> = emptyList(),
    val investmentHoldings: List<InvestmentHolding> = emptyList(),
    val recurringReminderOccurrences: List<RecurringReminderOccurrence> = emptyList(),
    val userMessage: String? = null
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GoogleSheetsRepository(application)
    private val savedThemeMode = repository.loadThemeMode()
    private val savedCurrency = repository.loadCurrency()
    private val savedIncludeTransfersInReports = repository.loadIncludeTransfersInReports()
    private val savedAccountBalances = repository.loadAccountBalances()
    private val savedTransactionTemplates = repository.loadTransactionTemplates()
    private val savedSavingsGoals = repository.loadSavingsGoals()
    private val savedExchangeRates = repository.loadExchangeRates()
    private val savedExchangeConversionEnabled = repository.loadExchangeConversionEnabled()
    private val savedBiometricLockEnabled = repository.loadBiometricLockEnabled()
    private val savedDashboardCardPreferences = repository.loadDashboardCardPreferences()
    private val savedCategoryRolloverSettings = repository.loadCategoryRolloverSettings()
    private val savedMonthlyCloseNotes = repository.loadMonthlyCloseNotes()
    private val savedCsvImportMapping = repository.loadCsvImportMapping()
    private val savedDebtAccounts = repository.loadDebtAccounts()
    private val savedInvestmentHoldings = repository.loadInvestmentHoldings()
    private val savedRecurringReminderOccurrences = repository.loadRecurringReminderOccurrences()

    private val _uiState = MutableStateFlow(
        FinanceTrackerUiState(
            currentMonthSheet = repository.getCurrentMonthSheetName(),
            syncStatus = buildSyncStatus(isUsingCachedData = false),
            themeMode = savedThemeMode,
            currency = savedCurrency,
            includeTransfersInReports = savedIncludeTransfersInReports,
            accountBalances = savedAccountBalances,
            transactionTemplates = savedTransactionTemplates,
            savingsGoals = savedSavingsGoals,
            exchangeRates = savedExchangeRates,
            exchangeConversionEnabled = savedExchangeConversionEnabled,
            biometricLockEnabled = savedBiometricLockEnabled,
            dashboardCardPreferences = savedDashboardCardPreferences,
            categoryRolloverSettings = savedCategoryRolloverSettings,
            monthlyCloseNotes = savedMonthlyCloseNotes,
            csvImportMapping = savedCsvImportMapping,
            debtAccounts = savedDebtAccounts,
            investmentHoldings = savedInvestmentHoldings,
            recurringReminderOccurrences = savedRecurringReminderOccurrences
        )
    )
    val uiState: StateFlow<FinanceTrackerUiState> = _uiState.asStateFlow()

    init {
        setPreferredCurrency(savedCurrency)
        hydrateFromCache()
        refreshAllData()
    }

    fun refreshData() {
        refreshAllData()
    }

    fun selectMonth(period: String) {
        val normalizedPeriod = runCatching { YearMonth.parse(period.trim()).toString() }
            .getOrDefault(period.trim())
        if (normalizedPeriod.isBlank()) return

        val selectedSheet = repository.getSheetNameForPeriod(normalizedPeriod)
        if (_uiState.value.currentMonthSheet == selectedSheet && !_uiState.value.isLoading) return

        if (!repository.isReadyForLiveSync()) {
            val allCachedExpenses = _uiState.value.reportExpenses.ifEmpty { _uiState.value.expenses }
            val selectedExpenses = allCachedExpenses
                .filter { YearMonth.from(it.date).toString() == normalizedPeriod }
                .sortedByDescending { it.date }
            updateLocalState(
                _uiState.value.copy(
                    currentMonthSheet = selectedSheet,
                    expenses = selectedExpenses,
                    monthlyIncome = incomeForPeriod(_uiState.value.incomeEntries, selectedSheet),
                    totalAmount = selectedExpenses.spendingTotal(),
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        val allCachedExpenses = _uiState.value.reportExpenses.ifEmpty { _uiState.value.expenses }
        val selectedExpenses = allCachedExpenses
            .filter { YearMonth.from(it.date).toString() == normalizedPeriod }
            .sortedByDescending { it.date }
        updateLocalState(
            _uiState.value.copy(
                currentMonthSheet = selectedSheet,
                expenses = selectedExpenses,
                monthlyIncome = incomeForPeriod(_uiState.value.incomeEntries, selectedSheet),
                totalAmount = selectedExpenses.spendingTotal()
            )
        )
        refreshAllData()
    }

    fun loadExpenses() {
        refreshAllData()
    }

    fun loadCategories() {
        refreshAllData()
    }

    fun addExpense(expense: Expense) {
        val targetSheet = sheetNameForExpense(expense)
        if (!repository.isReadyForLiveSync()) {
            val isSelectedMonth = targetSheet == currentSheetName()
            val newExpenses = if (isSelectedMonth) _uiState.value.expenses + expense else _uiState.value.expenses
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
            runCatching {
                repository.addExpense(targetSheet, expense)
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
            val selectedSheet = currentSheetName()
            val selectedMonthExpenses = normalizedExpenses.filter { sheetNameForExpense(it) == selectedSheet }
            val newExpenses = (_uiState.value.expenses + selectedMonthExpenses)
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
            runCatching {
                normalizedExpenses.forEach { expense ->
                    repository.addExpense(sheetNameForExpense(expense), expense)
                }
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to save transaction group.")
            }
        }
    }

    fun updateExpense(expense: Expense) {
        val originalExpense = _uiState.value.expenses.find { it.id == expense.id }
            ?: _uiState.value.reportExpenses.find { it.id == expense.id }
        val originalSheet = originalExpense?.let(::sheetNameForExpense) ?: currentSheetName()
        val targetSheet = sheetNameForExpense(expense)
        if (!repository.isReadyForLiveSync()) {
            val newExpenses = (_uiState.value.expenses.filterNot { it.id == expense.id } +
                listOf(expense).filter { sheetNameForExpense(it) == currentSheetName() })
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
            runCatching {
                if (originalSheet == targetSheet) {
                    repository.updateExpense(targetSheet, expense)
                } else {
                    repository.deleteExpense(originalSheet, expense.id)
                    repository.addExpense(targetSheet, expense.copy(sheetRowIndex = -1))
                }
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to update expense in Google Sheets.")
            }
        }
    }

    fun deleteExpense(id: String) {
        val deletedExpense = _uiState.value.expenses.find { it.id == id } ?: _uiState.value.reportExpenses.find { it.id == id }
        val targetSheet = deletedExpense?.let(::sheetNameForExpense) ?: currentSheetName()
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
                    pendingUndoDelete = deletedExpense?.let { PendingUndoDelete(it, targetSheet) }
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.deleteExpense(targetSheet, id)
            }.onSuccess {
                refreshAllData(pendingUndoExpense = deletedExpense, pendingUndoSheet = targetSheet)
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
        if (_uiState.value.pendingUndoDelete == null) return
        updateLocalState(_uiState.value.copy(pendingUndoDelete = null))
    }

    fun consumeOverspendingAlert() {
        if (_uiState.value.overspendingAlert == null) return
        updateLocalState(_uiState.value.copy(overspendingAlert = null))
    }

    fun setThemeMode(themeMode: ThemeMode) {
        repository.saveThemeMode(themeMode)
        updateLocalState(_uiState.value.copy(themeMode = themeMode))
    }

    fun setCurrency(currency: Currency) {
        repository.saveCurrency(currency)
        setPreferredCurrency(currency)
        updateLocalState(_uiState.value.copy(currency = currency))
    }

    fun setIncludeTransfersInReports(includeTransfers: Boolean) {
        repository.saveIncludeTransfersInReports(includeTransfers)
        updateLocalState(_uiState.value.copy(includeTransfersInReports = includeTransfers))
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        repository.saveBiometricLockEnabled(enabled)
        updateLocalState(_uiState.value.copy(biometricLockEnabled = enabled))
    }

    fun setExchangeConversionEnabled(enabled: Boolean) {
        repository.saveExchangeConversionEnabled(enabled)
        updateLocalState(_uiState.value.copy(exchangeConversionEnabled = enabled))
    }

    fun setExchangeRate(currency: Currency, rateToPreferred: Double) {
        if (rateToPreferred <= 0.0) return
        val updatedRates = _uiState.value.exchangeRates + (currency.code to rateToPreferred)
        repository.saveExchangeRates(updatedRates)
        updateLocalState(_uiState.value.copy(exchangeRates = updatedRates))
    }

    fun setDashboardCardVisibility(cardId: String, visible: Boolean) {
        updateDashboardPreference(cardId) { it.copy(visible = visible) }
    }

    fun toggleDashboardCardCollapsed(cardId: String) {
        updateDashboardPreference(cardId) { it.copy(collapsed = !it.collapsed) }
    }

    fun moveDashboardCard(cardId: String, direction: Int) {
        if (direction == 0) return
        val preferences = _uiState.value.dashboardCardPreferences.ifEmpty {
            repository.defaultDashboardCardPreferences()
        }.sortedBy { it.sortOrder }.toMutableList()
        val index = preferences.indexOfFirst { it.id == cardId }
        val targetIndex = (index + direction).coerceIn(0, preferences.lastIndex)
        if (index < 0 || index == targetIndex) return
        val moved = preferences.removeAt(index)
        preferences.add(targetIndex, moved)
        val reordered = preferences.mapIndexed { order, preference -> preference.copy(sortOrder = order) }
        repository.saveDashboardCardPreferences(reordered)
        updateLocalState(_uiState.value.copy(dashboardCardPreferences = repository.loadDashboardCardPreferences()))
    }

    fun setCategoryRollover(category: String, enabled: Boolean, carryOverspend: Boolean = true) {
        val normalizedCategory = category.trim()
        if (normalizedCategory.isBlank()) return
        val settings = (_uiState.value.categoryRolloverSettings.filterNot {
            it.category.equals(normalizedCategory, ignoreCase = true)
        } + CategoryRolloverSetting(normalizedCategory, enabled, carryOverspend))
            .sortedBy { it.category.lowercase(Locale.getDefault()) }
        repository.saveCategoryRolloverSettings(settings)
        updateLocalState(_uiState.value.copy(categoryRolloverSettings = settings))
    }

    fun saveMonthlyCloseNote(period: String, notes: String) {
        val normalizedPeriod = runCatching { YearMonth.parse(period.trim()).toString() }.getOrDefault(period.trim())
        val note = MonthlyCloseNote(normalizedPeriod, notes.trim())
        repository.saveMonthlyCloseNote(note)
        updateLocalState(_uiState.value.copy(monthlyCloseNotes = repository.loadMonthlyCloseNotes()))
    }

    fun saveCsvImportMapping(mapping: CsvImportMapping) {
        repository.saveCsvImportMapping(mapping)
        updateLocalState(_uiState.value.copy(csvImportMapping = repository.loadCsvImportMapping()))
    }

    fun importCsvText(
        csvText: String,
        mapping: CsvImportMapping,
        defaultCategory: String,
        defaultAccount: String,
        defaultCurrency: Currency
    ) {
        val parsedExpenses = repository.parseCsvExpenses(
            csvText = csvText,
            mapping = mapping,
            defaultCategory = defaultCategory,
            defaultAccount = defaultAccount,
            defaultCurrency = defaultCurrency
        )
        val existing = _uiState.value.reportExpenses.ifEmpty { _uiState.value.expenses }
        val importableExpenses = parsedExpenses.filterNot { candidate ->
            existing.any { existingExpense ->
                existingExpense.date == candidate.date &&
                    existingExpense.category.equals(candidate.category, ignoreCase = true) &&
                    Currency.fromCode(existingExpense.currencyCode) == Currency.fromCode(candidate.currencyCode) &&
                    kotlin.math.abs(existingExpense.amount - candidate.amount) < 0.001 &&
                    existingExpense.description.equals(candidate.description, ignoreCase = true)
            }
        }

        saveCsvImportMapping(mapping)
        if (importableExpenses.isEmpty()) {
            updateLocalState(_uiState.value.copy(userMessage = "No new CSV transactions found."))
            return
        }
        addExpenseGroup(importableExpenses.map { it.copy(id = UUID.randomUUID().toString()) })
        updateLocalState(_uiState.value.copy(userMessage = "Imported ${importableExpenses.size} CSV transactions."))
    }

    fun bulkDeleteTransactions(ids: Set<String>) {
        if (ids.isEmpty()) return
        val state = _uiState.value
        val targets = (state.reportExpenses.ifEmpty { state.expenses }).filter { it.id in ids }
        if (targets.isEmpty()) return

        if (!repository.isReadyForLiveSync()) {
            val newExpenses = state.expenses.filterNot { it.id in ids }
            val newReportExpenses = state.reportExpenses.filterNot { it.id in ids }
            updateLocalState(
                state.copy(
                    expenses = newExpenses,
                    reportExpenses = newReportExpenses,
                    totalAmount = newExpenses.spendingTotal(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true),
                    errorMessage = repository.getConfigurationStatusMessage(),
                    userMessage = "Deleted ${targets.size} transactions."
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                targets.forEach { repository.deleteExpense(sheetNameForExpense(it), it.id) }
            }.onSuccess {
                refreshAllData()
                updateLocalState(_uiState.value.copy(userMessage = "Deleted ${targets.size} transactions."))
            }.onFailure { error ->
                applyError(error.message ?: "Failed to delete selected transactions.")
            }
        }
    }

    fun bulkUpdateTransactions(
        ids: Set<String>,
        category: String? = null,
        paymentMethod: String? = null,
        tags: List<String>? = null
    ) {
        if (ids.isEmpty()) return
        val state = _uiState.value
        val updatedReportExpenses = state.reportExpenses.ifEmpty { state.expenses }.map { expense ->
            if (expense.id !in ids) {
                expense
            } else {
                expense.copy(
                    category = category?.takeIf { it.isNotBlank() } ?: expense.category,
                    paymentMethod = paymentMethod?.takeIf { it.isNotBlank() } ?: expense.paymentMethod,
                    tags = tags ?: expense.tags
                )
            }
        }
        val updatedSelectedExpenses = updatedReportExpenses.filter {
            YearMonth.from(it.date).toString() == periodFromSheet(currentSheetName())
        }

        if (!repository.isReadyForLiveSync()) {
            updateLocalState(
                state.copy(
                    expenses = updatedSelectedExpenses.sortedByDescending { it.date },
                    reportExpenses = updatedReportExpenses.sortedByDescending { it.date },
                    totalAmount = updatedSelectedExpenses.spendingTotal(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true),
                    errorMessage = repository.getConfigurationStatusMessage(),
                    userMessage = "Updated ${ids.size} selected transactions."
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                updatedReportExpenses.filter { it.id in ids }.forEach { expense ->
                    repository.updateExpense(sheetNameForExpense(expense), expense)
                }
            }.onSuccess {
                refreshAllData()
                updateLocalState(_uiState.value.copy(userMessage = "Updated ${ids.size} selected transactions."))
            }.onFailure { error ->
                applyError(error.message ?: "Failed to update selected transactions.")
            }
        }
    }

    fun updateRecurringReminder(entry: RecurringEntry, enabled: Boolean, daysBefore: Int) {
        updateRecurringEntry(
            entry.copy(
                reminderEnabled = enabled,
                reminderDaysBefore = daysBefore.coerceIn(0, 14)
            )
        )
    }

    fun markRecurringOccurrenceSkipped(entry: RecurringEntry, period: String) {
        val normalizedPeriod = runCatching { YearMonth.parse(period).toString() }.getOrDefault(period)
        repository.markRecurringOccurrenceSkipped(entry.id, normalizedPeriod)
        updateLocalState(
            _uiState.value.copy(
                recurringReminderOccurrences = repository.loadRecurringReminderOccurrences(),
                userMessage = "${entry.title} skipped for $normalizedPeriod."
            )
        )
    }

    fun markRecurringOccurrencePaid(entry: RecurringEntry, period: String) {
        val normalizedPeriod = runCatching { YearMonth.parse(period).toString() }.getOrDefault(period)
        viewModelScope.launch {
            runCatching {
                repository.markRecurringOccurrencePaid(entry.id, normalizedPeriod)
            }.onSuccess {
                hydrateFromCache()
                updateLocalState(
                    _uiState.value.copy(
                        recurringReminderOccurrences = repository.loadRecurringReminderOccurrences(),
                        userMessage = "${entry.title} marked paid for $normalizedPeriod."
                    )
                )
            }.onFailure { error ->
                applyError(error.message ?: "Failed to mark recurring item as paid.")
            }
        }
    }

    fun applyGoalAutoContribution(totalSavings: Double) {
        if (totalSavings <= 0.0 || _uiState.value.savingsGoals.isEmpty()) return
        var remainingSavings = totalSavings
        val updatedGoals = _uiState.value.savingsGoals.map { goal ->
            if (remainingSavings <= 0.0) {
                goal
            } else {
                val missing = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
                val allocation = missing.coerceAtMost(remainingSavings)
                remainingSavings -= allocation
                goal.copy(currentAmount = goal.currentAmount + allocation)
            }
        }
        repository.saveSavingsGoals(updatedGoals)
        updateLocalState(_uiState.value.copy(savingsGoals = updatedGoals, userMessage = "Savings allocated to goals."))
    }

    fun addOrUpdateDebtAccount(debt: DebtAccount) {
        val normalized = debt.copy(
            id = debt.id.ifBlank { UUID.randomUUID().toString() },
            name = debt.name.trim(),
            dueDay = debt.dueDay.coerceIn(1, 31),
            currencyCode = Currency.fromCode(debt.currencyCode).code
        )
        if (normalized.name.isBlank() || normalized.currentBalance < 0.0) return
        val debts = (_uiState.value.debtAccounts.filterNot { it.id == normalized.id } + normalized)
            .sortedBy { it.dueDay }
        repository.saveDebtAccounts(debts)
        updateLocalState(_uiState.value.copy(debtAccounts = debts))
    }

    fun deleteDebtAccount(id: String) {
        val debts = _uiState.value.debtAccounts.filterNot { it.id == id }
        repository.saveDebtAccounts(debts)
        updateLocalState(_uiState.value.copy(debtAccounts = debts))
    }

    fun addOrUpdateInvestmentHolding(holding: InvestmentHolding) {
        val normalized = holding.copy(
            id = holding.id.ifBlank { UUID.randomUUID().toString() },
            name = holding.name.trim(),
            assetType = holding.assetType.ifBlank { "Other" },
            currencyCode = Currency.fromCode(holding.currencyCode).code
        )
        if (normalized.name.isBlank()) return
        val holdings = (_uiState.value.investmentHoldings.filterNot { it.id == normalized.id } + normalized)
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
        repository.saveInvestmentHoldings(holdings)
        updateLocalState(_uiState.value.copy(investmentHoldings = holdings))
    }

    fun deleteInvestmentHolding(id: String) {
        val holdings = _uiState.value.investmentHoldings.filterNot { it.id == id }
        repository.saveInvestmentHoldings(holdings)
        updateLocalState(_uiState.value.copy(investmentHoldings = holdings))
    }

    fun buildDataValidationIssues(): List<ValidationIssue> {
        val state = _uiState.value
        val allExpenses = state.reportExpenses.ifEmpty { state.expenses }
        val incomePeriods = state.incomeEntries.map { it.period }.toSet()
        val expensePeriods = allExpenses.map { YearMonth.from(it.date).toString() }.toSet()
        val categoryNames = state.categoryState.categories.map { it.name.lowercase(Locale.getDefault()) }.toSet()
        val issues = mutableListOf<ValidationIssue>()

        (expensePeriods - incomePeriods).sortedDescending().forEach { period ->
            issues += ValidationIssue("Missing income", "$period has expenses but no monthly income entry.", "Warning")
        }
        allExpenses.filter { it.category.isBlank() }.forEach {
            issues += ValidationIssue("Missing category", "${it.date} ${it.description.ifBlank { it.amount.toString() }} has no category.", "Error")
        }
        allExpenses.filter { it.currencyCode.isBlank() }.forEach {
            issues += ValidationIssue("Missing currency", "${it.date} ${it.category} has no currency.", "Error")
        }
        allExpenses.groupBy {
            "${it.date}|${it.amount}|${it.currencyCode}|${it.category.lowercase(Locale.getDefault())}|${it.description.lowercase(Locale.getDefault())}"
        }.filterValues { it.size > 1 }.forEach { (_, duplicates) ->
            issues += ValidationIssue("Possible duplicate", "${duplicates.size} similar ${duplicates.first().category} transactions on ${duplicates.first().date}.", "Warning")
        }
        state.categoryBudgets.filter { it.category.lowercase(Locale.getDefault()) !in categoryNames }.forEach {
            issues += ValidationIssue("Unknown budget category", "${it.category} budget exists for ${it.period}, but the category is not configured.", "Warning")
        }
        state.recurringEntries.filter { it.dayOfMonth !in 1..31 }.forEach {
            issues += ValidationIssue("Invalid recurring date", "${it.title} has day ${it.dayOfMonth}.", "Error")
        }
        if (repository.isReadyForLiveSync() && state.syncStatus.lastSyncError != null) {
            issues += ValidationIssue("Google Sheets sync issue", state.syncStatus.lastSyncError, "Error")
        }
        return issues
    }

    fun buildSetupChecks(): List<SetupCheck> {
        val configMessage = repository.getConfigurationStatusMessage()
        val syncStatus = _uiState.value.syncStatus
        return listOf(
            SetupCheck(
                title = "Spreadsheet ID",
                detail = if (configMessage?.contains("Spreadsheet ID") == true) configMessage else "Spreadsheet ID is configured.",
                passed = configMessage?.contains("Spreadsheet ID") != true
            ),
            SetupCheck(
                title = "Service account JSON",
                detail = if (configMessage?.contains("service-account-key.json") == true) configMessage else "Service account credentials are available.",
                passed = configMessage?.contains("service-account-key.json") != true
            ),
            SetupCheck(
                title = "Authentication and sheet sharing",
                detail = syncStatus.lastSyncError ?: if (syncStatus.lastSuccessfulSyncMillis != null) "Last sync completed successfully." else "Run Refresh Data to verify access.",
                passed = syncStatus.lastSuccessfulSyncMillis != null && syncStatus.lastSyncError == null
            ),
            SetupCheck(
                title = "Required app data",
                detail = "Loaded ${_uiState.value.categoryState.categories.size} categories, ${_uiState.value.incomeEntries.size} income rows, and ${_uiState.value.recurringEntries.size} recurring rows.",
                passed = _uiState.value.categoryState.categories.isNotEmpty()
            )
        )
    }

    fun addTransactionTemplate(template: TransactionTemplate) {
        val normalized = template.copy(
            id = template.id.ifBlank { UUID.randomUUID().toString() },
            name = template.name.trim(),
            currencyCode = Currency.fromCode(template.currencyCode).code
        )
        if (normalized.name.isBlank() || normalized.category.isBlank() || normalized.amount <= 0.0) return
        val templates = (_uiState.value.transactionTemplates.filterNot { it.id == normalized.id } + normalized)
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
        repository.saveTransactionTemplates(templates)
        updateLocalState(_uiState.value.copy(transactionTemplates = templates))
    }

    fun deleteTransactionTemplate(templateId: String) {
        val templates = _uiState.value.transactionTemplates.filterNot { it.id == templateId }
        repository.saveTransactionTemplates(templates)
        updateLocalState(_uiState.value.copy(transactionTemplates = templates))
    }

    fun addOrUpdateSavingsGoal(goal: SavingsGoal) {
        val normalized = goal.copy(
            id = goal.id.ifBlank { UUID.randomUUID().toString() },
            name = goal.name.trim(),
            currencyCode = Currency.fromCode(goal.currencyCode).code
        )
        if (normalized.name.isBlank() || normalized.targetAmount <= 0.0) return
        val goals = (_uiState.value.savingsGoals.filterNot { it.id == normalized.id } + normalized)
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
        repository.saveSavingsGoals(goals)
        updateLocalState(_uiState.value.copy(savingsGoals = goals))
    }

    fun deleteSavingsGoal(goalId: String) {
        val goals = _uiState.value.savingsGoals.filterNot { it.id == goalId }
        repository.saveSavingsGoals(goals)
        updateLocalState(_uiState.value.copy(savingsGoals = goals))
    }

    fun addOrUpdateAccountBalance(name: String, amount: Double, isDebt: Boolean) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return
        val updatedBalances = _uiState.value.accountBalances
            .filterNot { it.name.equals(normalizedName, ignoreCase = true) } +
            AccountBalance(normalizedName, amount, isDebt)
        repository.saveAccountBalances(updatedBalances.sortedBy { it.name.lowercase(Locale.getDefault()) })
        updateLocalState(_uiState.value.copy(accountBalances = repository.loadAccountBalances()))
    }

    fun removeAccountBalance(name: String) {
        val updatedBalances = _uiState.value.accountBalances
            .filterNot { it.name.equals(name, ignoreCase = true) }
        repository.saveAccountBalances(updatedBalances)
        updateLocalState(_uiState.value.copy(accountBalances = updatedBalances))
    }

    fun exportData() {
        runCatching {
            val expenses = _uiState.value.reportExpenses.ifEmpty { _uiState.value.expenses }
            val incomeEntries = _uiState.value.incomeEntries
            val csvPath = repository.exportTransactionsCsv(expenses, incomeEntries)
            val pdfPath = repository.exportSummaryPdf(expenses, incomeEntries)
            "$csvPath and $pdfPath"
        }.onSuccess { path ->
            updateLocalState(_uiState.value.copy(userMessage = "Export saved: $path"))
        }.onFailure { error ->
            updateLocalState(_uiState.value.copy(userMessage = error.message ?: "Failed to export data."))
        }
    }

    fun backupData() {
        runCatching { repository.backupCacheToFile() }
            .onSuccess { path -> updateLocalState(_uiState.value.copy(userMessage = "Backup saved: $path")) }
            .onFailure { error -> updateLocalState(_uiState.value.copy(userMessage = error.message ?: "Failed to back up data.")) }
    }

    fun restoreLatestBackup() {
        if (repository.restoreLatestBackup()) {
            hydrateFromCache()
            updateLocalState(
                _uiState.value.copy(
                    accountBalances = repository.loadAccountBalances(),
                    includeTransfersInReports = repository.loadIncludeTransfersInReports(),
                    transactionTemplates = repository.loadTransactionTemplates(),
                    savingsGoals = repository.loadSavingsGoals(),
                    exchangeRates = repository.loadExchangeRates(),
                    exchangeConversionEnabled = repository.loadExchangeConversionEnabled(),
                    biometricLockEnabled = repository.loadBiometricLockEnabled(),
                    dashboardCardPreferences = repository.loadDashboardCardPreferences(),
                    categoryRolloverSettings = repository.loadCategoryRolloverSettings(),
                    monthlyCloseNotes = repository.loadMonthlyCloseNotes(),
                    csvImportMapping = repository.loadCsvImportMapping(),
                    debtAccounts = repository.loadDebtAccounts(),
                    investmentHoldings = repository.loadInvestmentHoldings(),
                    recurringReminderOccurrences = repository.loadRecurringReminderOccurrences(),
                    userMessage = "Latest backup restored."
                )
            )
        } else {
            updateLocalState(_uiState.value.copy(userMessage = "No backup file found."))
        }
    }

    fun consumeUserMessage() {
        if (_uiState.value.userMessage == null) return
        updateLocalState(_uiState.value.copy(userMessage = null))
    }

    fun setMonthlyIncome(amount: Double) {
        setMonthlyIncomeForPeriod(periodFromSheet(currentSheetName()), amount)
    }

    fun clearTransactionsForDay(date: LocalDate) {
        val targetSheet = repository.getSheetNameForPeriod(YearMonth.from(date).toString())

        if (!repository.isReadyForLiveSync()) {
            val newExpenses = _uiState.value.expenses.filterNot { it.date == date }
            val newReportExpenses = _uiState.value.reportExpenses.filterNot { it.date == date }
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
            runCatching {
                repository.clearExpensesForDay(targetSheet, date)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to clear transactions for the selected day.")
            }
        }
    }

    fun clearTransactionsForSelectedMonth() {
        val selectedSheet = currentSheetName()
        val selectedPeriod = periodFromSheet(selectedSheet)

        if (!repository.isReadyForLiveSync()) {
            val newReportExpenses = _uiState.value.reportExpenses.filterNot {
                YearMonth.from(it.date).toString() == selectedPeriod
            }
            updateLocalState(
                _uiState.value.copy(
                    expenses = emptyList(),
                    reportExpenses = newReportExpenses,
                    totalAmount = 0.0,
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.clearExpensesForMonth(selectedSheet)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to clear transactions for this month.")
            }
        }
    }

    fun clearAllTransactions() {
        if (!repository.isReadyForLiveSync()) {
            updateLocalState(
                _uiState.value.copy(
                    expenses = emptyList(),
                    reportExpenses = emptyList(),
                    totalAmount = 0.0,
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.clearAllExpenses()
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to clear all transactions.")
            }
        }
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
        updateRecurringEntry(updatedEntry)
    }

    fun updateRecurringEntry(entry: RecurringEntry) {
        if (!repository.isReadyForLiveSync()) {
            updateLocalState(
                _uiState.value.copy(
                    recurringEntries = _uiState.value.recurringEntries.map {
                        if (it.id == entry.id) entry else it
                    },
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.updateRecurringEntry(entry)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to update recurring entry.")
            }
        }
    }

    fun deleteRecurringEntry(entry: RecurringEntry) {
        if (!repository.isReadyForLiveSync()) {
            updateLocalState(
                _uiState.value.copy(
                    recurringEntries = _uiState.value.recurringEntries.filterNot { it.id == entry.id },
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.deleteRecurringEntry(entry.id)
            }.onSuccess {
                refreshAllData()
            }.onFailure { error ->
                applyError(error.message ?: "Failed to delete recurring entry.")
            }
        }
    }

    /**
     * Update category budget for a specific month.
     * @param name Category name
     * @param period Period in YYYY-MM format
     * @param amount Budget amount (null or 0 to remove budget)
     */
    fun updateCategoryBudgetForMonth(name: String, period: String, amount: Double?) {
        val normalizedName = name.trim()
        val normalizedPeriod = runCatching { YearMonth.parse(period.trim()).toString() }.getOrDefault(period.trim())
        if (normalizedName.isBlank()) return

        val existingState = _uiState.value
        
        // Update local state immediately
        val updatedBudgets = existingState.categoryBudgets.filter { 
            !(it.category.equals(normalizedName, ignoreCase = true) && it.period == normalizedPeriod)
        }.toMutableList()
        
        if (amount != null && amount > 0.0) {
            updatedBudgets.add(
                CategoryBudget(
                    id = "${normalizedName}_$normalizedPeriod",
                    category = normalizedName,
                    period = normalizedPeriod,
                    amount = amount
                )
            )
        }
        
        val optimisticState = existingState.copy(
            categoryBudgets = updatedBudgets,
            errorMessage = null
        )

        if (!repository.isReadyForLiveSync()) {
            updateLocalState(
                optimisticState.copy(
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        updateLocalState(
            optimisticState.copy(syncStatus = buildSyncStatus(isUsingCachedData = false))
        )

        viewModelScope.launch {
            runCatching {
                repository.upsertCategoryBudget(normalizedName, normalizedPeriod, amount)
            }.onSuccess { didSave ->
                if (didSave) {
                    refreshAllData()
                } else {
                    updateLocalState(
                        optimisticState.copy(
                            syncStatus = buildSyncStatus(isUsingCachedData = true),
                            errorMessage = "Failed to save category budget to Google Sheets."
                        )
                    )
                }
            }.onFailure { error ->
                updateLocalState(
                    optimisticState.copy(
                        syncStatus = buildSyncStatus(isUsingCachedData = true),
                        errorMessage = error.message
                            ?: "Cannot connect to spreadsheet. Budget saved locally."
                    )
                )
            }
        }
    }

    /**
     * Update the color for a category. This applies globally for all instances.
     */
    fun updateCategoryColor(name: String, newColor: String) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return

        val existingState = _uiState.value
        val updatedCategories = existingState.categoryState.categories.map {
            if (it.name.equals(normalizedName, ignoreCase = true)) {
                it.copy(color = newColor)
            } else {
                it
            }
        }
        val categoryState = existingState.categoryState.copy(categories = updatedCategories)
        val optimisticState = existingState.copy(
            categoryState = categoryState,
            errorMessage = null
        )

        if (!repository.isReadyForLiveSync()) {
            updateLocalState(
                optimisticState.copy(
                    errorMessage = repository.getConfigurationStatusMessage(),
                    syncStatus = buildSyncStatus(isUsingCachedData = true)
                )
            )
            return
        }

        updateLocalState(optimisticState.copy(syncStatus = buildSyncStatus(isUsingCachedData = false)))

        viewModelScope.launch {
            runCatching {
                repository.updateCategoryColor(normalizedName, newColor)
            }.onSuccess { didSave ->
                if (didSave) {
                    refreshAllData()
                } else {
                    updateLocalState(
                        optimisticState.copy(
                            syncStatus = buildSyncStatus(isUsingCachedData = true),
                            errorMessage = "Failed to save category color to Google Sheets."
                        )
                    )
                }
            }.onFailure { error ->
                updateLocalState(
                    optimisticState.copy(
                        syncStatus = buildSyncStatus(isUsingCachedData = true),
                        errorMessage = error.message
                            ?: "Cannot connect to spreadsheet. Color saved locally."
                    )
                )
            }
        }
    }

    /**
     * Legacy method - redirects to updateCategoryBudgetForMonth for current month.
     */
    fun updateCategoryBudget(name: String, monthlyBudget: Double?) {
        updateCategoryBudgetForMonth(name, periodFromSheetName(currentSheetName()), monthlyBudget)
    }

    fun addCategory(name: String, color: String) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return

        val category = Category(
            name = normalizedName,
            color = color.takeIf { it.isNotBlank() } ?: suggestedColorFor(normalizedName),
            icon = "default"
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

    private fun updateDashboardPreference(
        cardId: String,
        transform: (DashboardCardPreference) -> DashboardCardPreference
    ) {
        val preferences = _uiState.value.dashboardCardPreferences.ifEmpty {
            repository.defaultDashboardCardPreferences()
        }
        val updated = preferences.map { preference ->
            if (preference.id == cardId) transform(preference) else preference
        }
        repository.saveDashboardCardPreferences(updated)
        updateLocalState(_uiState.value.copy(dashboardCardPreferences = repository.loadDashboardCardPreferences()))
    }

    private fun hydrateFromCache() {
        val cachedData = repository.loadCachedData() ?: return
        val currentSheet = cachedData.currentMonthSheet.ifBlank { repository.getCurrentMonthSheetName() }
        val currentPeriod = periodFromSheet(currentSheet)
        val categories = mergeWithDefaultCategories(
            cachedData.categories.ifEmpty { repository.getDefaultCategories() }
        )
        val reportExpenses = cachedData.reportExpenses.ifEmpty { cachedData.expenses }
        val selectedExpenses = reportExpenses
            .filter { YearMonth.from(it.date).toString() == currentPeriod }
            .ifEmpty { cachedData.expenses.filter { YearMonth.from(it.date).toString() == currentPeriod } }
        val stateFromCache = _uiState.value.copy(
            currentMonthSheet = currentSheet,
            expenses = selectedExpenses.sortedByDescending { it.date },
            reportExpenses = reportExpenses.sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.modifiedAt }),
            incomeEntries = cachedData.incomeEntries.sortedByDescending { it.period },
            recurringEntries = cachedData.recurringEntries.sortedBy { it.dayOfMonth },
            monthlyIncome = incomeForPeriod(cachedData.incomeEntries, currentSheet),
            categoryBudgets = cachedData.categoryBudgets,
            categoryState = CategoryState(categories = categories, isLoading = false),
            totalAmount = selectedExpenses.spendingTotal(),
            syncStatus = buildSyncStatus(isUsingCachedData = true),
            errorMessage = null
        )
        _uiState.value = stateFromCache
    }

    private fun refreshAllData(pendingUndoExpense: Expense? = null, pendingUndoSheet: String = currentSheetName()) {
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

            val currentSheet = existingState.currentMonthSheet.ifBlank { repository.getCurrentMonthSheetName() }
            val currentPeriod = periodFromSheet(currentSheet)
            val isLiveSyncReady = repository.isReadyForLiveSync()

            if (!isLiveSyncReady) {
                val categories = mergeWithDefaultCategories(
                    existingState.categoryState.categories.ifEmpty { repository.getDefaultCategories() }
                )
                val allCachedExpenses = existingState.reportExpenses.ifEmpty { existingState.expenses }
                val selectedExpenses = allCachedExpenses.filter { YearMonth.from(it.date).toString() == currentPeriod }
                updateLocalState(
                    existingState.copy(
                        currentMonthSheet = currentSheet,
                        expenses = selectedExpenses.sortedByDescending { it.date },
                        categoryState = CategoryState(categories = categories, isLoading = false),
                        monthlyIncome = incomeForPeriod(existingState.incomeEntries, currentSheet),
                        totalAmount = selectedExpenses.spendingTotal(),
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
                val categoryBudgetsDeferred = async { runCatching { repository.fetchAllCategoryBudgets() } }

                RefreshResults(
                    categories = categoriesDeferred.await(),
                    expenses = expensesDeferred.await(),
                    reportExpenses = reportExpensesDeferred.await(),
                    incomeEntries = incomeEntriesDeferred.await(),
                    recurringEntries = recurringDeferred.await(),
                    categoryBudgets = categoryBudgetsDeferred.await()
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

            if (currentSheet == repository.getCurrentMonthSheetName()) {
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
                    categoryBudgets = refreshResults.categoryBudgets.getOrDefault(existingState.categoryBudgets),
                    expenses = expenses.sortedByDescending { it.date },
                    reportExpenses = reportExpenses.sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.modifiedAt }),
                    incomeEntries = incomeEntries.sortedByDescending { it.period },
                    recurringEntries = recurringEntries.sortedBy { it.dayOfMonth },
                    monthlyIncome = incomeForPeriod(incomeEntries, currentSheet),
                    totalAmount = expenses.spendingTotal(),
                    isLoading = false,
                    errorMessage = firstLoadError,
                    syncStatus = buildSyncStatus(isUsingCachedData = firstLoadError != null),
                    pendingUndoDelete = pendingUndoExpense?.let { PendingUndoDelete(it, pendingUndoSheet) },
                    overspendingAlert = buildOverspendingAlert(
                        categories = categories,
                        monthlyIncome = incomeForPeriod(incomeEntries, currentSheet),
                        totalAmount = expenses.spendingTotal(),
                        expenses = expenses,
                        period = currentPeriod
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
                recurringEntries = newState.recurringEntries,
                categoryBudgets = newState.categoryBudgets
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
                    expenses = _uiState.value.expenses,
                    period = periodFromSheet(_uiState.value.currentMonthSheet)
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

    private fun sheetNameForExpense(expense: Expense): String {
        return repository.getSheetNameForPeriod(YearMonth.from(expense.date).toString())
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

    private fun periodFromSheetName(sheetName: String): String {
        return periodFromSheet(sheetName)
    }

    private fun buildOverspendingAlert(
        categories: List<Category>,
        monthlyIncome: Double,
        totalAmount: Double,
        expenses: List<Expense>,
        period: String
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
        val categoryBudgetsByCategory = _uiState.value.categoryBudgets
            .filter { it.period == period }
            .associateBy { it.category }
        
        val overspentCategory = categories.firstOrNull { category ->
            val budget = categoryBudgetsByCategory[category.name]?.amount ?: 0.0
            budget > 0.0 && (spendByCategory[category.name] ?: 0.0) > budget
        }

        return overspentCategory?.let { category ->
            val spent = spendByCategory[category.name] ?: 0.0
            val budget = categoryBudgetsByCategory[category.name]?.amount
            OverspendingAlert(
                title = "${category.name} budget exceeded",
                message = "${category.name} spending is ${spent.toDisplayAmount()} against ${budget?.toDisplayAmount().orEmpty()}."
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
        currencyCode: String,
        splitRows: List<SplitExpenseInput>
    ): List<Expense> {
        val splitGroupId = UUID.randomUUID().toString()
        return splitRows.mapNotNull { row ->
            val amount = row.amount.takeIf { it > 0.0 } ?: return@mapNotNull null
            Expense(
                id = UUID.randomUUID().toString(),
                date = date,
                amount = amount,
                currencyCode = Currency.fromCode(currencyCode).code,
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
        val recurringEntries: Result<List<RecurringEntry>>,
        val categoryBudgets: Result<List<CategoryBudget>>
    )
}

data class SplitExpenseInput(
    val category: String,
    val amount: Double,
    val subcategory: String? = null
)
