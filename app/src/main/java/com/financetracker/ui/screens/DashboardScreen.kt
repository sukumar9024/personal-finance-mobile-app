package com.financetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financetracker.data.model.AccountBalance
import com.financetracker.data.model.Category
import com.financetracker.data.model.CategoryBudget
import com.financetracker.data.model.CategoryRolloverSetting
import com.financetracker.data.model.Currency
import com.financetracker.data.model.DashboardCardPreference
import com.financetracker.data.model.Expense
import com.financetracker.data.model.RecurringEntry
import com.financetracker.data.model.RecurringType
import com.financetracker.data.model.SavingsGoal
import com.financetracker.data.model.TransactionType
import com.financetracker.data.model.isTransfer
import com.financetracker.data.model.spendingTotal
import com.financetracker.ui.theme.AnimatedProgressBar
import com.financetracker.ui.theme.BadgeChip
import com.financetracker.ui.theme.CardElevation
import com.financetracker.ui.theme.IconCircle
import com.financetracker.ui.theme.ScreenPadding
import com.financetracker.ui.theme.SectionHeader
import com.financetracker.ui.theme.Shapes
import com.financetracker.ui.theme.Spacing
import com.financetracker.ui.theme.categoryColor
import com.financetracker.ui.theme.formatCurrency
import com.financetracker.ui.theme.formatCurrencyRounded
import com.financetracker.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private enum class TransactionSortOption(val label: String) {
    NEWEST("Newest"),
    HIGHEST("Highest"),
    LOWEST("Lowest"),
    CATEGORY("Category"),
    ACCOUNT("Account")
}

private data class MonthSavingsSummary(
    val month: YearMonth,
    val income: Double,
    val spending: Double
) {
    val savings: Double get() = income - spending
    val savingsRate: Double get() = if (income > 0.0) (savings / income) * 100.0 else 0.0
}

private data class CurrencyExpenseSummary(
    val currency: Currency,
    val spending: Double,
    val transactionCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onAddExpenseClick: () -> Unit,
    onExpenseClick: (Expense) -> Unit,
    onCategoriesClick: () -> Unit,
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All categories") }
    var selectedAccountFilter by remember { mutableStateOf("All accounts") }
    var selectedCurrencyFilter by remember { mutableStateOf("All currencies") }
    var minAmountInput by remember { mutableStateOf("") }
    var maxAmountInput by remember { mutableStateOf("") }
    var tagFilter by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("All types") }
    var receiptFilter by remember { mutableStateOf("Any receipt") }
    var recurringFilter by remember { mutableStateOf("Any source") }
    var overBudgetOnly by remember { mutableStateOf(false) }
    var selectedDashboardGroup by remember { mutableStateOf("Overview") }
    var selectedSort by remember { mutableStateOf(TransactionSortOption.NEWEST) }
    var selectedTransactionIds by remember { mutableStateOf(setOf<String>()) }
    var showBulkEditDialog by remember { mutableStateOf(false) }
    var quickAddAmount by remember { mutableStateOf("") }
    var quickAddCategory by remember { mutableStateOf("") }
    var quickAddAccount by remember { mutableStateOf("Cash") }
    var quickAddCurrency by remember { mutableStateOf(uiState.currency) }
    var incomeInput by remember { mutableStateOf("") }
    var startDateInput by remember { mutableStateOf("") }
    var endDateInput by remember { mutableStateOf("") }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showNetWorthDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showExchangeDialog by remember { mutableStateOf(false) }
    var clearAction by remember { mutableStateOf<ClearAction?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currency = uiState.currency
    val visibleMonthExpenses = if (uiState.includeTransfersInReports) {
        uiState.expenses
    } else {
        uiState.expenses.filterNot { it.isTransfer }
    }
    val historyExpenses = if (uiState.includeTransfersInReports) {
        uiState.reportExpenses.ifEmpty { uiState.expenses }
    } else {
        uiState.reportExpenses.ifEmpty { uiState.expenses }.filterNot { it.isTransfer }
    }
    val primaryCurrencyExpenses = visibleMonthExpenses.filter { Currency.fromCode(it.currencyCode) == currency }
    val averageSpend = if (primaryCurrencyExpenses.isNotEmpty()) {
        primaryCurrencyExpenses.sumOf { it.amount } / primaryCurrencyExpenses.size
    } else {
        0.0
    }
    val spendByCategory = primaryCurrencyExpenses
        .groupBy { it.category }
        .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    val currentMonthText = uiState.currentMonthSheet
        .removePrefix("expenses_")
        .replace("_", " / ")
    val selectedMonth = parseCurrentPeriodFromSheet(uiState.currentMonthSheet)
    val categoryBudgetsByCategory = uiState.categoryBudgets
        .filter { it.period == parseCurrentPeriodFromSheet(uiState.currentMonthSheet).toString() }
        .associateBy { it.category }
    val effectiveBudgetsByCategory = buildEffectiveBudgets(
        budgets = uiState.categoryBudgets,
        expenses = historyExpenses,
        rolloverSettings = uiState.categoryRolloverSettings,
        selectedMonth = selectedMonth
    )
    val highlightedCategories = uiState.categoryState.categories
        .sortedWith(
            compareByDescending<Category> { spendByCategory[it.name] ?: 0.0 }
                .thenByDescending { categoryBudgetsByCategory[it.name]?.amount ?: 0.0 }
                .thenBy { it.name }
        )
        .take(4)
    val selectedQuickAddDate = LocalDate.now().let { today ->
        if (YearMonth.from(today) == selectedMonth) {
            today
        } else {
            selectedMonth.atDay(today.dayOfMonth.coerceAtMost(selectedMonth.lengthOfMonth()))
        }
    }
    val clearDates = uiState.expenses
        .map { it.date }
        .distinct()
        .sortedDescending()
    val errorMessage = uiState.errorMessage
    val selectedMonthSpending = primaryCurrencyExpenses.sumOf { it.amount }
    val currencyExpenseSummaries = visibleMonthExpenses
        .groupBy { Currency.fromCode(it.currencyCode) }
        .map { (expenseCurrency, entries) ->
            CurrencyExpenseSummary(
                currency = expenseCurrency,
                spending = entries.sumOf { it.amount },
                transactionCount = entries.size
            )
        }
        .sortedByDescending { it.spending }
    val convertedMonthSpending = convertedSpending(
        expenses = visibleMonthExpenses,
        preferredCurrency = currency,
        rates = uiState.exchangeRates,
        enabled = uiState.exchangeConversionEnabled
    )
    val remainingAmount = uiState.monthlyIncome - selectedMonthSpending
    val savingsSummaries = buildMonthlySavingsSummaries(
        expenses = historyExpenses.filter { Currency.fromCode(it.currencyCode) == currency },
        incomeEntries = uiState.incomeEntries,
        selectedMonth = selectedMonth,
        selectedMonthIncome = uiState.monthlyIncome
    )
    val selectedSavingsSummary = savingsSummaries.firstOrNull { it.month == selectedMonth }
        ?: MonthSavingsSummary(selectedMonth, uiState.monthlyIncome, selectedMonthSpending)
    val totalSavings = savingsSummaries.sumOf { it.savings }
    val allTimeIncome = savingsSummaries.sumOf { it.income }
    val allTimeSavingsRate = if (allTimeIncome > 0.0) (totalSavings / allTimeIncome) * 100.0 else 0.0
    val bestSavingsMonth = savingsSummaries.maxByOrNull { it.savings }
    val worstSavingsMonth = savingsSummaries.minByOrNull { it.savings }
    val negativeSavingsMonths = savingsSummaries.count { it.savings < 0.0 }
    val currentDay = if (YearMonth.now() == selectedMonth) LocalDate.now().dayOfMonth else selectedMonth.lengthOfMonth()
    val daysElapsed = currentDay.coerceAtLeast(1)
    val remainingDays = (selectedMonth.lengthOfMonth() - currentDay).coerceAtLeast(0)
    val dailyBurnRate = selectedMonthSpending / daysElapsed
    val projectedSpending = selectedMonthSpending + (dailyBurnRate * remainingDays)
    val safeToSpendPerDay = if (remainingDays > 0) (uiState.monthlyIncome - selectedMonthSpending) / remainingDays else uiState.monthlyIncome - selectedMonthSpending
    val paymentSummary = primaryCurrencyExpenses.groupBy { it.paymentMethod }.mapValues { (_, entries) -> entries.sumOf { it.amount } }
    val upcomingRecurring = upcomingRecurringEntries(uiState.recurringEntries, selectedMonth)
    val parsedIncome = incomeInput.toDoubleOrNull()
    val canSaveIncome = parsedIncome != null && parsedIncome != uiState.monthlyIncome
    LaunchedEffect(uiState.monthlyIncome) {
        incomeInput = uiState.monthlyIncome.toEditableAmount()
    }

    LaunchedEffect(uiState.userMessage) {
        val message = uiState.userMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeUserMessage()
    }

    val categoryFilterOptions = listOf("All categories") + uiState.categoryState.categories.map { it.name }
    val currencyFilterOptions = listOf("All currencies") + Currency.entries.map { it.code }
    val typeFilterOptions = listOf("All types", "Expenses", "Transfers", "Split")
    val receiptFilterOptions = listOf("Any receipt", "Has receipt", "No receipt")
    val recurringFilterOptions = listOf("Any source", "Recurring", "Manual")
    val accountOptions = accountOptions()
    if (quickAddCategory.isBlank() && uiState.categoryState.categories.isNotEmpty()) {
        quickAddCategory = uiState.categoryState.categories.first().name
    }
    val parsedStartDate = runCatching { LocalDate.parse(startDateInput) }.getOrNull()
    val parsedEndDate = runCatching { LocalDate.parse(endDateInput) }.getOrNull()
    val parsedMinAmount = minAmountInput.toDoubleOrNull()
    val parsedMaxAmount = maxAmountInput.toDoubleOrNull()
    val filteredExpenses = visibleMonthExpenses.filter { expense ->
        val matchesQuery = searchQuery.isBlank() || listOf(
            expense.category,
            expense.subcategory.orEmpty(),
            expense.description,
            expense.amount.toString(),
            expense.paymentMethod,
            expense.tags.joinToString(" ")
        ).any { it.contains(searchQuery, ignoreCase = true) }
        val matchesCategory = selectedCategoryFilter == "All categories" || expense.category == selectedCategoryFilter
        val matchesAccount = selectedAccountFilter == "All accounts" || expense.paymentMethod == selectedAccountFilter
        val matchesCurrency = selectedCurrencyFilter == "All currencies" || expense.currencyCode == selectedCurrencyFilter
        val matchesStartDate = parsedStartDate == null || !expense.date.isBefore(parsedStartDate)
        val matchesEndDate = parsedEndDate == null || !expense.date.isAfter(parsedEndDate)
        val matchesMinAmount = parsedMinAmount == null || expense.amount >= parsedMinAmount
        val matchesMaxAmount = parsedMaxAmount == null || expense.amount <= parsedMaxAmount
        val requestedTags = tagFilter.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val matchesTags = requestedTags.isEmpty() || requestedTags.all { requested ->
            expense.tags.any { it.equals(requested, ignoreCase = true) }
        }
        val matchesType = when (selectedTypeFilter) {
            "Expenses" -> !expense.isTransfer && expense.splitGroupId == null
            "Transfers" -> expense.isTransfer
            "Split" -> expense.splitGroupId != null
            else -> true
        }
        val matchesReceipt = when (receiptFilter) {
            "Has receipt" -> !expense.receiptUrl.isNullOrBlank()
            "No receipt" -> expense.receiptUrl.isNullOrBlank()
            else -> true
        }
        val matchesRecurring = when (recurringFilter) {
            "Recurring" -> expense.recurringEntryId != null
            "Manual" -> expense.recurringEntryId == null
            else -> true
        }
        val matchesBudget = !overBudgetOnly || isExpenseOverEffectiveBudget(expense, visibleMonthExpenses, effectiveBudgetsByCategory)
        matchesQuery && matchesCategory && matchesAccount && matchesCurrency && matchesStartDate && matchesEndDate &&
            matchesMinAmount && matchesMaxAmount && matchesTags && matchesType && matchesReceipt && matchesRecurring && matchesBudget
    }.sortedWith(sortComparator(selectedSort))
    val quickAddDuplicate = quickAddAmount.toDoubleOrNull()?.let { amount ->
        uiState.expenses.firstOrNull { expense ->
            expense.date == selectedQuickAddDate &&
                expense.category == quickAddCategory &&
                Currency.fromCode(expense.currencyCode) == quickAddCurrency &&
                expense.paymentMethod == quickAddAccount &&
                kotlin.math.abs(expense.amount - amount) < 0.001
        }
    }
    val lastSyncedText = uiState.syncStatus.lastSuccessfulSyncMillis?.let(::formatTimestamp)
        ?: "No successful sync yet"
    val lastAttemptText = uiState.syncStatus.lastSyncAttemptMillis?.let(::formatTimestamp)
        ?: "No refresh attempts yet"
    val refreshLabel = if (uiState.syncStatus.lastSyncError != null) "Retry Sync" else "Refresh Data"
    val dashboardGroups = listOf("Overview", "Calendar", "Goals", "Data")

    LaunchedEffect(uiState.pendingUndoDelete?.token) {
        val pendingUndo = uiState.pendingUndoDelete ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "${pendingUndo.expense.category} deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short
        )
        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
            viewModel.restoreDeletedExpense()
        } else {
            viewModel.clearPendingUndoDelete()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Finance Tracker",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentMonthText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onReportsClick) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Reports"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = Shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ScreenPadding,
                end = ScreenPadding,
                top = paddingValues.calculateTopPadding() + Spacing.sm,
                bottom = paddingValues.calculateBottomPadding() + Spacing.xxxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item {
                DashboardGroupSelector(
                    groups = dashboardGroups,
                    selectedGroup = selectedDashboardGroup,
                    onGroupSelected = { selectedDashboardGroup = it }
                )
            }

            item {
                DashboardFeatureContainer("monthly_overview", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    BalanceCard(
                        monthlyIncome = uiState.monthlyIncome,
                        totalAmount = selectedMonthSpending,
                        remainingAmount = remainingAmount,
                        transactionCount = visibleMonthExpenses.size,
                        averageSpend = averageSpend,
                        currency = currency
                    )
                }
            }

            item {
                DashboardFeatureContainer("savings_dashboard", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    SavingsDashboardCard(
                        selectedSummary = selectedSavingsSummary,
                        totalSavings = totalSavings,
                        allTimeSavingsRate = allTimeSavingsRate,
                        bestMonth = bestSavingsMonth,
                        worstMonth = worstSavingsMonth,
                        negativeSavingsMonths = negativeSavingsMonths,
                        monthlySummaries = savingsSummaries.takeLast(6),
                        currency = currency
                    )
                }
            }

            item {
                DashboardFeatureContainer("spending_calendar", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    CalendarSpendingCard(
                        selectedMonth = selectedMonth,
                        expenses = visibleMonthExpenses,
                        preferredCurrency = currency,
                        conversionEnabled = uiState.exchangeConversionEnabled,
                        exchangeRates = uiState.exchangeRates,
                        onClearDay = { date ->
                            clearAction = ClearAction.Day(
                                date = date,
                                count = uiState.expenses.count { it.date == date }
                            )
                        }
                    )
                }
            }

            item {
                DashboardFeatureContainer("multi_currency", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    MultiCurrencyExpenditureCard(
                        selectedMonth = selectedMonth,
                        summaries = currencyExpenseSummaries
                    )
                }
            }

            item {
                DashboardFeatureContainer("exchange_conversion", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    ExchangeConversionCard(
                        preferredCurrency = currency,
                        convertedSpending = convertedMonthSpending,
                        enabled = uiState.exchangeConversionEnabled,
                        onToggle = viewModel::setExchangeConversionEnabled,
                        onConfigure = { showExchangeDialog = true }
                    )
                }
            }

            item {
                DashboardFeatureContainer("operating_view", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    UsefulDashboardCard(
                        selectedMonth = selectedMonth,
                        dailyBurnRate = dailyBurnRate,
                        projectedSpending = projectedSpending,
                        safeToSpendPerDay = safeToSpendPerDay,
                        paymentSummary = paymentSummary,
                        upcomingRecurring = upcomingRecurring,
                        currency = currency
                    )
                }
            }

            item {
                DashboardFeatureContainer("net_worth", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    NetWorthCard(
                        balances = uiState.accountBalances,
                        currency = currency,
                        onEditClick = { showNetWorthDialog = true }
                    )
                }
            }

            item {
                DashboardFeatureContainer("savings_goals", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    SavingsGoalsCard(
                        goals = uiState.savingsGoals,
                        suggestedSavings = selectedSavingsSummary.savings.coerceAtLeast(0.0),
                        onAutoAllocate = { viewModel.applyGoalAutoContribution(selectedSavingsSummary.savings) },
                        onEditClick = { showGoalDialog = true }
                    )
                }
            }

            item {
                DashboardFeatureContainer("month_data", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    DashboardControlCard(
                        currentMonthText = currentMonthText,
                        selectedMonth = selectedMonth,
                        onMonthChange = { viewModel.selectMonth(it.toString()) },
                        onOpenMonthPicker = { showMonthPicker = true },
                        incomeInput = incomeInput,
                        onIncomeInputChange = { value ->
                            incomeInput = value.filter { it.isDigit() || it == '.' }
                        },
                        onSaveIncome = {
                            parsedIncome?.let(viewModel::setMonthlyIncome)
                        },
                        canSaveIncome = canSaveIncome && !uiState.isLoading,
                        entryCount = visibleMonthExpenses.size,
                        categoryCount = uiState.categoryState.categories.size,
                        isLoading = uiState.isLoading,
                        onRefreshClick = viewModel::refreshData,
                        syncStatus = uiState.syncStatus,
                        lastSyncedText = lastSyncedText,
                        lastAttemptText = lastAttemptText,
                        refreshLabel = refreshLabel,
                        currency = currency,
                        clearDates = clearDates,
                        includeTransfers = uiState.includeTransfersInReports,
                        onIncludeTransfersChange = viewModel::setIncludeTransfersInReports,
                        onExportData = viewModel::exportData,
                        onBackupData = viewModel::backupData,
                        onRestoreBackup = viewModel::restoreLatestBackup,
                        onClearDay = { date ->
                            clearAction = ClearAction.Day(
                                date = date,
                                count = uiState.expenses.count { it.date == date }
                            )
                        },
                        onClearMonth = {
                            clearAction = ClearAction.Month(
                                month = selectedMonth,
                                count = uiState.expenses.size
                            )
                        },
                        onClearAll = {
                            clearAction = ClearAction.All(
                                count = uiState.reportExpenses.ifEmpty { uiState.expenses }.size
                            )
                        }
                    )
                }
            }

            item {
                DashboardFeatureContainer("quick_add", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    QuickAddCard(
                        amount = quickAddAmount,
                        onAmountChange = { quickAddAmount = it.filter { char -> char.isDigit() || char == '.' } },
                        categoryOptions = uiState.categoryState.categories.map { it.name },
                        selectedCategory = quickAddCategory,
                        onCategorySelected = { quickAddCategory = it },
                        accountOptions = accountOptions.drop(1),
                        selectedAccount = quickAddAccount,
                        onAccountSelected = { quickAddAccount = it },
                        selectedCurrency = quickAddCurrency,
                        onCurrencySelected = { quickAddCurrency = it },
                        duplicateExpense = quickAddDuplicate,
                        onSave = {
                            quickAddAmount.toDoubleOrNull()?.let { amount ->
                                val expense = Expense(
                                    date = selectedQuickAddDate,
                                    amount = amount,
                                    currencyCode = quickAddCurrency.code,
                                    category = quickAddCategory.ifBlank { "Other" },
                                    paymentMethod = quickAddAccount
                                )
                                viewModel.addExpense(expense)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Quick expense saved")
                                }
                                quickAddAmount = ""
                            }
                        },
                        enabled = quickAddAmount.toDoubleOrNull() != null && quickAddCategory.isNotBlank()
                    )
                }
            }

            if (errorMessage != null) {
                item {
                    ErrorBanner(message = errorMessage)
                }
            }

            item {
                DashboardFeatureContainer("quick_actions", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    QuickActionsRow(
                        onReportsClick = onReportsClick,
                        onCategoriesClick = onCategoriesClick,
                        onSettingsClick = onSettingsClick
                    )
                }
            }

            if (highlightedCategories.isNotEmpty() && dashboardCardVisible("top_categories", selectedDashboardGroup, uiState.dashboardCardPreferences)) {
                item {
                    DashboardFeatureContainer("top_categories", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            SectionHeader(
                                title = "Top Categories",
                                subtitle = "Where your spending is going this month",
                                action = {
                                    Text(
                                        text = "See all",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier
                                            .clickable(onClick = onCategoriesClick)
                                            .padding(Spacing.sm)
                                    )
                                }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                highlightedCategories.forEach { category ->
                                    val amount = uiState.expenses
                                        .filter { it.category == category.name }
                                        .sumOf { it.amount }
                                    CategoryMiniCard(
                                        category = category,
                                        amount = amount,
                                        currency = currency,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.expenses.isEmpty() && !uiState.isLoading) {
                item {
                    OnboardingCard(
                        onQuickAdd = { quickAddAmount = "" },
                        onAddExpense = onAddExpenseClick,
                        onReportsClick = onReportsClick
                    )
                }
            }

            item {
                DashboardFeatureContainer("transactions", selectedDashboardGroup, uiState.dashboardCardPreferences) {
                    TransactionFilterCard(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        categoryOptions = categoryFilterOptions,
                        selectedCategory = selectedCategoryFilter,
                        onCategorySelected = { selectedCategoryFilter = it },
                        accountOptions = accountOptions,
                        selectedAccount = selectedAccountFilter,
                        onAccountSelected = { selectedAccountFilter = it },
                        currencyOptions = currencyFilterOptions,
                        selectedCurrency = selectedCurrencyFilter,
                        onCurrencySelected = { selectedCurrencyFilter = it },
                        minAmountInput = minAmountInput,
                        onMinAmountInputChange = { minAmountInput = it.filter { char -> char.isDigit() || char == '.' } },
                        maxAmountInput = maxAmountInput,
                        onMaxAmountInputChange = { maxAmountInput = it.filter { char -> char.isDigit() || char == '.' } },
                        tagFilter = tagFilter,
                        onTagFilterChange = { tagFilter = it },
                        typeOptions = typeFilterOptions,
                        selectedType = selectedTypeFilter,
                        onTypeSelected = { selectedTypeFilter = it },
                        receiptOptions = receiptFilterOptions,
                        selectedReceipt = receiptFilter,
                        onReceiptSelected = { receiptFilter = it },
                        recurringOptions = recurringFilterOptions,
                        selectedRecurring = recurringFilter,
                        onRecurringSelected = { recurringFilter = it },
                        overBudgetOnly = overBudgetOnly,
                        onOverBudgetOnlyChange = { overBudgetOnly = it },
                        startDateInput = startDateInput,
                        onStartDateInputChange = { startDateInput = it },
                        endDateInput = endDateInput,
                        onEndDateInputChange = { endDateInput = it },
                        sortOptions = TransactionSortOption.entries,
                        selectedSort = selectedSort,
                        onSortSelected = { selectedSort = it }
                    )
                }
            }

            item {
                if (dashboardCardVisible("transactions", selectedDashboardGroup, uiState.dashboardCardPreferences)) {
                    SectionHeader(
                        title = "Recent Transactions",
                        subtitle = "${filteredExpenses.size} matching transactions this month"
                    )
                }
            }

            if (dashboardCardVisible("transactions", selectedDashboardGroup, uiState.dashboardCardPreferences) && filteredExpenses.isNotEmpty()) {
                item {
                    BulkActionsCard(
                        selectedCount = selectedTransactionIds.size,
                        selectedTotalByCurrency = filteredExpenses.filter { it.id in selectedTransactionIds }
                            .groupBy { Currency.fromCode(it.currencyCode) }
                            .mapValues { (_, entries) -> entries.sumOf { it.amount } },
                        allVisibleCount = filteredExpenses.size,
                        onSelectAll = { selectedTransactionIds = filteredExpenses.map { it.id }.toSet() },
                        onClearSelection = { selectedTransactionIds = emptySet() },
                        onEditSelected = { showBulkEditDialog = true },
                        onDeleteSelected = {
                            viewModel.bulkDeleteTransactions(selectedTransactionIds)
                            selectedTransactionIds = emptySet()
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xxxl),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (dashboardCardVisible("transactions", selectedDashboardGroup, uiState.dashboardCardPreferences) && filteredExpenses.isEmpty() && !uiState.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.xxxl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconCircle(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                tint = MaterialTheme.colorScheme.primary,
                                size = 64.dp,
                                iconSize = 32.dp
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text(
                                text = if (uiState.expenses.isEmpty()) "No expenses yet" else "No matching transactions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = if (uiState.expenses.isEmpty()) {
                                    "Tap the + button to add your first expense and start tracking"
                                } else {
                                    "Try another search or clear one of the filters"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(Spacing.lg))
                            FloatingActionButton(
                                onClick = onAddExpenseClick,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Expense")
                            }
                        }
                    }
                }
            }

            if (dashboardCardVisible("transactions", selectedDashboardGroup, uiState.dashboardCardPreferences) && filteredExpenses.isNotEmpty()) {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        categoryColor = categoryColor(
                            uiState.categoryState.categories.find { it.name == expense.category }
                        ),
                        selected = expense.id in selectedTransactionIds,
                        onSelectionChange = { checked ->
                            selectedTransactionIds = if (checked) selectedTransactionIds + expense.id else selectedTransactionIds - expense.id
                        },
                        onClick = {
                            if (selectedTransactionIds.isNotEmpty()) {
                                selectedTransactionIds = if (expense.id in selectedTransactionIds) {
                                    selectedTransactionIds - expense.id
                                } else {
                                    selectedTransactionIds + expense.id
                                }
                            } else {
                                onExpenseClick(expense)
                            }
                        }
                    )
                }
            }
        }
    }

    clearAction?.let { action ->
        AlertDialog(
            onDismissRequest = { clearAction = null },
            title = { Text(action.title()) },
            text = { Text(action.message()) },
            confirmButton = {
                Button(
                    onClick = {
                        when (action) {
                            is ClearAction.Day -> viewModel.clearTransactionsForDay(action.date)
                            is ClearAction.Month -> viewModel.clearTransactionsForSelectedMonth()
                            is ClearAction.All -> viewModel.clearAllTransactions()
                        }
                        clearAction = null
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { clearAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            selectedMonth = selectedMonth,
            availableMonths = savingsSummaries.map { it.month },
            onMonthSelected = {
                viewModel.selectMonth(it.toString())
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    if (showNetWorthDialog) {
        NetWorthDialog(
            balances = uiState.accountBalances,
            currency = currency,
            onSave = { name, amount, isDebt ->
                viewModel.addOrUpdateAccountBalance(name, amount, isDebt)
            },
            onRemove = viewModel::removeAccountBalance,
            onDismiss = { showNetWorthDialog = false }
        )
    }

    if (showGoalDialog) {
        SavingsGoalDialog(
            goals = uiState.savingsGoals,
            defaultCurrency = currency,
            onSave = viewModel::addOrUpdateSavingsGoal,
            onDelete = viewModel::deleteSavingsGoal,
            onDismiss = { showGoalDialog = false }
        )
    }

    if (showExchangeDialog) {
        ExchangeRateDialog(
            preferredCurrency = currency,
            rates = uiState.exchangeRates,
            onSaveRate = viewModel::setExchangeRate,
            onDismiss = { showExchangeDialog = false }
        )
    }

    if (showBulkEditDialog) {
        BulkEditDialog(
            categories = uiState.categoryState.categories.map { it.name }.ifEmpty { listOf("Other") },
            accountOptions = accountOptions.drop(1),
            onSave = { category, account, tags ->
                viewModel.bulkUpdateTransactions(
                    ids = selectedTransactionIds,
                    category = category.takeIf { it != "No change" },
                    paymentMethod = account.takeIf { it != "No change" },
                    tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
                )
                selectedTransactionIds = emptySet()
                showBulkEditDialog = false
            },
            onDismiss = { showBulkEditDialog = false }
        )
    }
}

private sealed interface ClearAction {
    data class Day(val date: LocalDate, val count: Int) : ClearAction
    data class Month(val month: YearMonth, val count: Int) : ClearAction
    data class All(val count: Int) : ClearAction
}

private fun ClearAction.title(): String {
    return when (this) {
        is ClearAction.Day -> "Clear Day"
        is ClearAction.Month -> "Clear Month"
        is ClearAction.All -> "Clear All Data"
    }
}

private fun ClearAction.message(): String {
    return when (this) {
        is ClearAction.Day -> "Clear $count transactions from ${date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))}?"
        is ClearAction.Month -> "Clear $count transactions from ${month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))}?"
        is ClearAction.All -> "Clear $count transactions from every month? Categories, monthly income, budgets, and settings will stay."
    }
}

@Composable
private fun DashboardGroupSelector(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelected: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        groups.forEach { group ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onGroupSelected(group) },
                shape = Shapes.full,
                color = if (selectedGroup == group) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Text(
                    text = group,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedGroup == group) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DashboardFeatureContainer(
    cardId: String,
    selectedGroup: String,
    preferences: List<DashboardCardPreference>,
    content: @Composable () -> Unit
) {
    val preference = preferences.firstOrNull { it.id == cardId }
        ?: DashboardCardPreference(cardId, cardId.replace("_", " ").replaceFirstChar { it.uppercase() })
    if (!preference.visible || preference.group != selectedGroup) return

    content()
}

private fun dashboardCardVisible(cardId: String, selectedGroup: String, preferences: List<DashboardCardPreference>): Boolean {
    val preference = preferences.firstOrNull { it.id == cardId } ?: return true
    return preference.visible && preference.group == selectedGroup
}

@Composable
private fun BulkActionsCard(
    selectedCount: Int,
    selectedTotalByCurrency: Map<Currency, Double>,
    allVisibleCount: Int,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onEditSelected: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Bulk Actions",
                subtitle = if (selectedCount == 0) "Select transactions to edit or delete together" else "$selectedCount selected of $allVisibleCount"
            )
            if (selectedTotalByCurrency.isNotEmpty()) {
                selectedTotalByCurrency.forEach { (currency, total) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(currency.code, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text(formatCurrencyRounded(total, currency), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = onSelectAll, modifier = Modifier.weight(1f), shape = Shapes.medium) {
                    Text("Select All")
                }
                OutlinedButton(onClick = onClearSelection, enabled = selectedCount > 0, modifier = Modifier.weight(1f), shape = Shapes.medium) {
                    Text("Clear")
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Button(onClick = onEditSelected, enabled = selectedCount > 0, modifier = Modifier.weight(1f), shape = Shapes.medium) {
                    Text("Edit Selected")
                }
                OutlinedButton(onClick = onDeleteSelected, enabled = selectedCount > 0, modifier = Modifier.weight(1f), shape = Shapes.medium) {
                    Text("Delete Selected")
                }
            }
        }
    }
}

@Composable
private fun BulkEditDialog(
    categories: List<String>,
    accountOptions: List<String>,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var category by remember { mutableStateOf("No change") }
    var account by remember { mutableStateOf("No change") }
    var tags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Selected Transactions") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterDropdown(
                    label = "Category",
                    selectedValue = category,
                    options = listOf("No change") + categories,
                    onOptionSelected = { category = it }
                )
                FilterDropdown(
                    label = "Payment account",
                    selectedValue = account,
                    options = listOf("No change") + accountOptions,
                    onOptionSelected = { account = it }
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Replace tags, comma separated") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(category, account, tags) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun BalanceCard(
    monthlyIncome: Double,
    totalAmount: Double,
    remainingAmount: Double,
    transactionCount: Int,
    averageSpend: Double,
    currency: Currency = Currency.getDefault()
) {
    val progress = if (monthlyIncome > 0.0) {
        (totalAmount / monthlyIncome).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shapes.extraLarge)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(Spacing.xxl)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monthly Income",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(Spacing.xxs))
                        Text(
                            text = formatCurrency(monthlyIncome, currency),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (monthlyIncome > 0.0) {
                                "Income saved for this month"
                            } else {
                                "Set this month's income below"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                    BadgeChip(
                        text = "$transactionCount transactions",
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))

                if (monthlyIncome > 0.0) {
                    AnimatedProgressBar(
                        progress = progress,
                        color = if (remainingAmount < 0) Color(0xFFFFD0D0) else Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = Shapes.medium,
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatCurrency(totalAmount, currency),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = Shapes.medium,
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatCurrency(remainingAmount, currency),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = Shapes.medium,
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.md),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Avg / Txn",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatCurrencyRounded(averageSpend, currency),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsDashboardCard(
    selectedSummary: MonthSavingsSummary,
    totalSavings: Double,
    allTimeSavingsRate: Double,
    bestMonth: MonthSavingsSummary?,
    worstMonth: MonthSavingsSummary?,
    negativeSavingsMonths: Int,
    monthlySummaries: List<MonthSavingsSummary>,
    currency: Currency
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Savings Dashboard",
                subtitle = "All-time savings and monthly saved amount from history"
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                StatusMetric("Saved So Far", formatCurrencyRounded(totalSavings, currency), Modifier.weight(1f))
                StatusMetric("This Month", formatCurrencyRounded(selectedSummary.savings, currency), Modifier.weight(1f))
                StatusMetric("Savings Rate", String.format(Locale.US, "%.1f%%", selectedSummary.savingsRate), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                StatusMetric("All-Time Rate", String.format(Locale.US, "%.1f%%", allTimeSavingsRate), Modifier.weight(1f))
                StatusMetric("Best Month", bestMonth?.month?.format(DateTimeFormatter.ofPattern("MMM yy")).orEmpty(), Modifier.weight(1f))
                StatusMetric("Over Budget", negativeSavingsMonths.toString(), Modifier.weight(1f))
            }
            if (monthlySummaries.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    monthlySummaries.forEach { summary ->
                        val isNegative = summary.savings < 0.0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = summary.month.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Income ${formatCurrencyRounded(summary.income, currency)} • Spent ${formatCurrencyRounded(summary.spending, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatCurrencyRounded(summary.savings, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            worstMonth?.takeIf { it.savings < 0.0 }?.let {
                Text(
                    text = "Worst month: ${it.month.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()))} at ${formatCurrencyRounded(it.savings, currency)}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CalendarSpendingCard(
    selectedMonth: YearMonth,
    expenses: List<Expense>,
    preferredCurrency: Currency,
    conversionEnabled: Boolean,
    exchangeRates: Map<String, Double>,
    onClearDay: (LocalDate) -> Unit
) {
    val expensesByDay = expenses.groupBy { it.date.dayOfMonth }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val dayIntensityTotals = (1..selectedMonth.lengthOfMonth()).associateWith { day ->
        val dayExpenses = expensesByDay[day].orEmpty()
        convertedSpending(dayExpenses, preferredCurrency, exchangeRates, conversionEnabled)
            ?: dayExpenses.filter { Currency.fromCode(it.currencyCode) == preferredCurrency }.sumOf { it.amount }
    }
    val maxDayTotal = dayIntensityTotals.values.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Spending Calendar",
                subtitle = "Heatmap for ${selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))}"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                HeatLegendChip("None", 0f)
                HeatLegendChip("Low", 0.25f)
                HeatLegendChip("Medium", 0.55f)
                HeatLegendChip("High", 0.9f)
            }
            (1..selectedMonth.lengthOfMonth()).chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    week.forEach { day ->
                        val dayExpenses = expensesByDay[day].orEmpty()
                        val preferredTotal = dayExpenses
                            .filter { Currency.fromCode(it.currencyCode) == preferredCurrency }
                            .sumOf { it.amount }
                        val intensity = ((dayIntensityTotals[day] ?: 0.0) / maxDayTotal).toFloat().coerceIn(0f, 1f)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .clickable(enabled = dayExpenses.isNotEmpty()) {
                                    selectedDate = selectedMonth.atDay(day)
                                },
                            shape = Shapes.medium,
                            color = if (dayExpenses.isNotEmpty()) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f + (0.54f * intensity))
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.xs),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(day.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                if (dayExpenses.isNotEmpty()) {
                                    Text(
                                        text = if (preferredTotal > 0.0) formatCurrencyRounded(preferredTotal, preferredCurrency) else "${dayExpenses.size} tx",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f).height(58.dp))
                    }
                }
            }
        }
    }

    selectedDate?.let { date ->
        val dayExpenses = expenses.filter { it.date == date }
        DayDetailDialog(
            date = date,
            expenses = dayExpenses,
            preferredCurrency = preferredCurrency,
            onClearDay = {
                onClearDay(date)
                selectedDate = null
            },
            onDismiss = { selectedDate = null }
        )
    }
}

@Composable
private fun HeatLegendChip(label: String, intensity: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(Shapes.full)
                .background(
                    if (intensity == 0f) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f + (0.54f * intensity))
                    }
                )
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DayDetailDialog(
    date: LocalDate,
    expenses: List<Expense>,
    preferredCurrency: Currency,
    onClearDay: () -> Unit,
    onDismiss: () -> Unit
) {
    val totalsByCurrency = expenses
        .groupBy { Currency.fromCode(it.currencyCode) }
        .mapValues { (_, entries) -> entries.sumOf { it.amount } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("${expenses.size} transactions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                totalsByCurrency.forEach { (currency, total) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(currency.displayName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text(formatCurrency(total, currency), style = MaterialTheme.typography.labelLarge)
                    }
                }
                expenses.take(8).forEach { expense ->
                    Surface(shape = Shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.category, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                Text(expense.description.ifBlank { expense.paymentMethod }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(formatCurrencyRounded(expense.amount, Currency.fromCode(expense.currencyCode)), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                if (expenses.any { Currency.fromCode(it.currencyCode) == preferredCurrency }) {
                    Text("Preferred-currency total is used for intensity when available.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(onClick = onClearDay) { Text("Clear Day") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ExchangeConversionCard(
    preferredCurrency: Currency,
    convertedSpending: Double?,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Exchange Conversion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Optional converted total in ${preferredCurrency.code}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                androidx.compose.material3.Switch(checked = enabled, onCheckedChange = onToggle)
            }
            Text(
                text = convertedSpending?.let { formatCurrency(it, preferredCurrency) } ?: "Add exchange rates to convert non-${preferredCurrency.code} spending.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedButton(onClick = onConfigure, modifier = Modifier.fillMaxWidth(), shape = Shapes.medium) {
                Text("Configure Exchange Rates")
            }
        }
    }
}

@Composable
private fun SavingsGoalsCard(
    goals: List<SavingsGoal>,
    suggestedSavings: Double,
    onAutoAllocate: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Savings Goals",
                subtitle = "Track emergency fund, loan payoff, trip, or investment targets",
                action = {
                    Text(
                        text = "Edit",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onEditClick).padding(Spacing.sm)
                    )
                }
            )
            if (goals.isEmpty()) {
                Text("No goals yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                OutlinedButton(
                    onClick = onAutoAllocate,
                    enabled = suggestedSavings > 0.0,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                ) {
                    Text("Auto-Allocate This Month's Savings")
                }
                goals.forEach { goal ->
                    val goalCurrency = Currency.fromCode(goal.currencyCode)
                    val progress = if (goal.targetAmount > 0.0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(goal.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text("${formatCurrencyRounded(goal.currentAmount, goalCurrency)} / ${formatCurrencyRounded(goal.targetAmount, goalCurrency)}", style = MaterialTheme.typography.labelMedium)
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(Shapes.full).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(progress).height(8.dp).clip(Shapes.full).background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiCurrencyExpenditureCard(
    selectedMonth: YearMonth,
    summaries: List<CurrencyExpenseSummary>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Expenditure By Currency",
                subtitle = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
            )
            if (summaries.isEmpty()) {
                Text(
                    text = "No expenses for this month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                summaries.forEach { summary ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = summary.currency.displayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${summary.transactionCount} transactions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatCurrency(summary.spending, summary.currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsefulDashboardCard(
    selectedMonth: YearMonth,
    dailyBurnRate: Double,
    projectedSpending: Double,
    safeToSpendPerDay: Double,
    paymentSummary: Map<String, Double>,
    upcomingRecurring: List<RecurringEntry>,
    currency: Currency
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Monthly Operating View",
                subtitle = "Burn rate, forecast, payment accounts, and upcoming recurring items"
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                StatusMetric("Daily Burn", formatCurrencyRounded(dailyBurnRate, currency), Modifier.weight(1f))
                StatusMetric("Projected", formatCurrencyRounded(projectedSpending, currency), Modifier.weight(1f))
                StatusMetric("Safe / Day", formatCurrencyRounded(safeToSpendPerDay, currency), Modifier.weight(1f))
            }
            if (paymentSummary.isNotEmpty()) {
                SectionHeader(title = "Payment Accounts", subtitle = "Spending split by payment method")
                paymentSummary.toList().sortedByDescending { it.second }.take(4).forEach { (account, amount) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(account, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text(formatCurrencyRounded(amount, currency), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            SectionHeader(
                title = "Upcoming",
                subtitle = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
            )
            if (upcomingRecurring.isEmpty()) {
                Text("No upcoming recurring entries for this month.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                upcomingRecurring.take(5).forEach { entry ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${entry.type.name.lowercase().replaceFirstChar { it.uppercase() }} on day ${entry.dayOfMonth}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(formatCurrencyRounded(entry.amount, currency), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun NetWorthCard(
    balances: List<AccountBalance>,
    currency: Currency,
    onEditClick: () -> Unit
) {
    val assets = balances.filterNot { it.isDebt }.sumOf { it.amount }
    val debts = balances.filter { it.isDebt }.sumOf { it.amount }
    val netWorth = assets - debts
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Net Worth",
                subtitle = "Manual account balances for assets and debt",
                action = {
                    Text(
                        text = "Edit",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onEditClick).padding(Spacing.sm)
                    )
                }
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                StatusMetric("Assets", formatCurrencyRounded(assets, currency), Modifier.weight(1f))
                StatusMetric("Debt", formatCurrencyRounded(debts, currency), Modifier.weight(1f))
                StatusMetric("Net", formatCurrencyRounded(netWorth, currency), Modifier.weight(1f))
            }
            if (balances.isEmpty()) {
                Text("Add balances for bank, cash, wallet, investments, debt, or loans.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onReportsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        QuickActionCard(
            title = "Reports",
            subtitle = "Trends & charts",
            icon = Icons.Default.Analytics,
            onClick = onReportsClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            title = "Categories",
            subtitle = "All categories",
            icon = Icons.Default.Category,
            onClick = onCategoriesClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            title = "Settings",
            subtitle = "Theme & help",
            icon = Icons.Default.Settings,
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DashboardControlCard(
    currentMonthText: String,
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    onOpenMonthPicker: () -> Unit,
    incomeInput: String,
    onIncomeInputChange: (String) -> Unit,
    onSaveIncome: () -> Unit,
    canSaveIncome: Boolean,
    entryCount: Int,
    categoryCount: Int,
    isLoading: Boolean,
    onRefreshClick: () -> Unit,
    syncStatus: com.financetracker.ui.viewmodel.SyncStatus,
    lastSyncedText: String,
    lastAttemptText: String,
    refreshLabel: String,
    currency: Currency = Currency.getDefault(),
    clearDates: List<LocalDate>,
    includeTransfers: Boolean,
    onIncludeTransfersChange: (Boolean) -> Unit,
    onExportData: () -> Unit,
    onBackupData: () -> Unit,
    onRestoreBackup: () -> Unit,
    onClearDay: (LocalDate) -> Unit,
    onClearMonth: () -> Unit,
    onClearAll: () -> Unit
) {
    var clearDayExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            SectionHeader(
                title = "Month, Income & Data",
                subtitle = "Edit this month without changing other months"
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onMonthChange(selectedMonth.minusMonths(1)) },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClick = onOpenMonthPicker)
                        )
                    }
                    IconButton(
                        onClick = { onMonthChange(selectedMonth.plusMonths(1)) },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = "Current Sync",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = currentMonthText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (syncStatus.isLiveSyncEnabled) {
                            if (syncStatus.isUsingCachedData) "Showing cached data" else "Live sync is active"
                        } else {
                            "Live sync is unavailable"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Last synced: $lastSyncedText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Last attempt: $lastAttemptText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    syncStatus.lastSyncError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                StatusMetric(
                    label = "Entries",
                    value = entryCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatusMetric(
                    label = "Categories",
                    value = categoryCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatusMetric(
                    label = "Status",
                    value = if (isLoading) "Syncing" else if (syncStatus.isUsingCachedData) "Cached" else "Live",
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = incomeInput,
                onValueChange = onIncomeInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly income for ${selectedMonth}") },
                prefix = { Text(currency.symbol) },
                singleLine = true,
                shape = Shapes.medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Button(
                    onClick = onSaveIncome,
                    enabled = canSaveIncome,
                    modifier = Modifier.weight(1f),
                    shape = Shapes.medium
                ) {
                    Text("Save Income")
                }

                OutlinedButton(
                    onClick = onRefreshClick,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = Shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(if (isLoading) "Refreshing" else refreshLabel)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Include transfers in reports", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Transfers stay excluded from savings by default.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = includeTransfers,
                            onCheckedChange = onIncludeTransfersChange
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        OutlinedButton(onClick = onExportData, modifier = Modifier.weight(1f), shape = Shapes.medium) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text("Export")
                        }
                        OutlinedButton(onClick = onBackupData, modifier = Modifier.weight(1f), shape = Shapes.medium) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text("Backup")
                        }
                    }
                    OutlinedButton(onClick = onRestoreBackup, modifier = Modifier.fillMaxWidth(), shape = Shapes.medium) {
                        Text("Restore Latest Backup")
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.large,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.16f)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Data Management",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { clearDayExpanded = true },
                                enabled = clearDates.isNotEmpty() && !isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                shape = Shapes.medium
                            ) {
                                Text("Clear Day")
                            }
                            DropdownMenu(
                                expanded = clearDayExpanded,
                                onDismissRequest = { clearDayExpanded = false }
                            ) {
                                clearDates.forEach { date ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())))
                                        },
                                        onClick = {
                                            clearDayExpanded = false
                                            onClearDay(date)
                                        }
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = onClearMonth,
                            enabled = entryCount > 0 && !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = Shapes.medium
                        ) {
                            Text("Clear Month")
                        }
                    }
                    OutlinedButton(
                        onClick = onClearAll,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = Shapes.medium
                    ) {
                        Text("Clear All Transactions")
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthPickerDialog(
    selectedMonth: YearMonth,
    availableMonths: List<YearMonth>,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    val months = (availableMonths + selectedMonth + YearMonth.now())
        .distinct()
        .sortedDescending()
        .take(18)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Month") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                months.forEach { month ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMonthSelected(month) },
                        shape = Shapes.medium,
                        color = if (month == selectedMonth) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }
                    ) {
                        Text(
                            text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(Spacing.md)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun NetWorthDialog(
    balances: List<AccountBalance>,
    currency: Currency,
    onSave: (String, Double, Boolean) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isDebt by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Net Worth") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account name") },
                    singleLine = true,
                    shape = Shapes.medium
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Balance") },
                    prefix = { Text(currency.symbol) },
                    singleLine = true,
                    shape = Shapes.medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("This is debt or a loan", style = MaterialTheme.typography.labelLarge)
                    androidx.compose.material3.Switch(checked = isDebt, onCheckedChange = { isDebt = it })
                }
                Button(
                    onClick = {
                        amount.toDoubleOrNull()?.let { parsedAmount ->
                            onSave(name, parsedAmount, isDebt)
                            name = ""
                            amount = ""
                            isDebt = false
                        }
                    },
                    enabled = name.isNotBlank() && amount.toDoubleOrNull() != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                ) {
                    Text("Save Balance")
                }
                balances.forEach { balance ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(balance.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                Text(if (balance.isDebt) "Debt" else "Asset", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(formatCurrencyRounded(balance.amount, currency), style = MaterialTheme.typography.labelLarge)
                            IconButton(onClick = { onRemove(balance.name) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove balance")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun SavingsGoalDialog(
    goals: List<SavingsGoal>,
    defaultCurrency: Currency,
    onSave: (SavingsGoal) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(defaultCurrency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Savings Goals") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal name") }, singleLine = true, shape = Shapes.medium)
                OutlinedTextField(value = target, onValueChange = { target = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Target amount") }, prefix = { Text(selectedCurrency.symbol) }, singleLine = true, shape = Shapes.medium)
                OutlinedTextField(value = current, onValueChange = { current = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Current saved") }, prefix = { Text(selectedCurrency.symbol) }, singleLine = true, shape = Shapes.medium)
                FilterDropdown(
                    label = "Currency",
                    selectedValue = "${selectedCurrency.code} (${selectedCurrency.symbol})",
                    options = Currency.entries.map { "${it.code} (${it.symbol})" },
                    onOptionSelected = { selected ->
                        Currency.entries.firstOrNull { selected.startsWith(it.code) }?.let { selectedCurrency = it }
                    }
                )
                Button(
                    onClick = {
                        onSave(
                            SavingsGoal(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                targetAmount = target.toDoubleOrNull() ?: 0.0,
                                currentAmount = current.toDoubleOrNull() ?: 0.0,
                                currencyCode = selectedCurrency.code
                            )
                        )
                        name = ""
                        target = ""
                        current = ""
                    },
                    enabled = name.isNotBlank() && target.toDoubleOrNull() != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                ) {
                    Text("Save Goal")
                }
                goals.forEach { goal ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(goal.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text("${formatCurrencyRounded(goal.currentAmount, Currency.fromCode(goal.currencyCode))} / ${formatCurrencyRounded(goal.targetAmount, Currency.fromCode(goal.currencyCode))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDelete(goal.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete goal")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun ExchangeRateDialog(
    preferredCurrency: Currency,
    rates: Map<String, Double>,
    onSaveRate: (Currency, Double) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exchange Rates") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("Enter how much 1 unit of each currency is worth in ${preferredCurrency.code}.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Currency.entries.filterNot { it == preferredCurrency }.forEach { currency ->
                    var rate by remember(currency.code, rates[currency.code]) { mutableStateOf(rates[currency.code]?.toEditableAmount().orEmpty()) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = rate,
                            onValueChange = { rate = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text("1 ${currency.code} in ${preferredCurrency.code}") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = Shapes.medium
                        )
                        Button(onClick = { rate.toDoubleOrNull()?.let { onSaveRate(currency, it) } }, enabled = rate.toDoubleOrNull() != null) {
                            Text("Save")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun TransactionFilterCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    categoryOptions: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    accountOptions: List<String>,
    selectedAccount: String,
    onAccountSelected: (String) -> Unit,
    currencyOptions: List<String>,
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    minAmountInput: String,
    onMinAmountInputChange: (String) -> Unit,
    maxAmountInput: String,
    onMaxAmountInputChange: (String) -> Unit,
    tagFilter: String,
    onTagFilterChange: (String) -> Unit,
    typeOptions: List<String>,
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    receiptOptions: List<String>,
    selectedReceipt: String,
    onReceiptSelected: (String) -> Unit,
    recurringOptions: List<String>,
    selectedRecurring: String,
    onRecurringSelected: (String) -> Unit,
    overBudgetOnly: Boolean,
    onOverBudgetOnlyChange: (Boolean) -> Unit,
    startDateInput: String,
    onStartDateInputChange: (String) -> Unit,
    endDateInput: String,
    onEndDateInputChange: (String) -> Unit,
    sortOptions: List<TransactionSortOption>,
    selectedSort: TransactionSortOption,
    onSortSelected: (TransactionSortOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            SectionHeader(
                title = "Search & Filters",
                subtitle = "Find transactions by text, category, currency, amount, tags, source, or budget status"
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search transactions") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = Shapes.medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FilterDropdown(
                    label = "Category",
                    selectedValue = selectedCategory,
                    options = categoryOptions,
                    onOptionSelected = onCategorySelected,
                    modifier = Modifier.weight(1f)
                )
                FilterDropdown(
                    label = "Account",
                    selectedValue = selectedAccount,
                    options = accountOptions,
                    onOptionSelected = onAccountSelected,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FilterDropdown(
                    label = "Currency",
                    selectedValue = selectedCurrency,
                    options = currencyOptions,
                    onOptionSelected = onCurrencySelected,
                    modifier = Modifier.weight(1f)
                )
                FilterDropdown(
                    label = "Type",
                    selectedValue = selectedType,
                    options = typeOptions,
                    onOptionSelected = onTypeSelected,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                OutlinedTextField(
                    value = minAmountInput,
                    onValueChange = onMinAmountInputChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Min amount") },
                    singleLine = true,
                    shape = Shapes.medium
                )
                OutlinedTextField(
                    value = maxAmountInput,
                    onValueChange = onMaxAmountInputChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Max amount") },
                    singleLine = true,
                    shape = Shapes.medium
                )
            }
            OutlinedTextField(
                value = tagFilter,
                onValueChange = onTagFilterChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tags, comma separated") },
                singleLine = true,
                shape = Shapes.medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FilterDropdown(
                    label = "Receipt",
                    selectedValue = selectedReceipt,
                    options = receiptOptions,
                    onOptionSelected = onReceiptSelected,
                    modifier = Modifier.weight(1f)
                )
                FilterDropdown(
                    label = "Source",
                    selectedValue = selectedRecurring,
                    options = recurringOptions,
                    onOptionSelected = onRecurringSelected,
                    modifier = Modifier.weight(1f)
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Only over-budget category transactions", style = MaterialTheme.typography.labelLarge)
                    androidx.compose.material3.Switch(
                        checked = overBudgetOnly,
                        onCheckedChange = onOverBudgetOnlyChange
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                OutlinedTextField(
                    value = startDateInput,
                    onValueChange = onStartDateInputChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("From YYYY-MM-DD") },
                    singleLine = true,
                    shape = Shapes.medium
                )
                OutlinedTextField(
                    value = endDateInput,
                    onValueChange = onEndDateInputChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("To YYYY-MM-DD") },
                    singleLine = true,
                    shape = Shapes.medium
                )
            }
            SortDropdown(
                options = sortOptions,
                selected = selectedSort,
                onSelected = onSortSelected
            )
        }
    }
}

@Composable
private fun QuickAddCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    categoryOptions: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    accountOptions: List<String>,
    selectedAccount: String,
    onAccountSelected: (String) -> Unit,
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit,
    duplicateExpense: Expense?,
    onSave: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            SectionHeader(
                title = "Quick Add",
                subtitle = "Save a transaction in a few taps"
            )
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                prefix = { Text(selectedCurrency.symbol) },
                singleLine = true,
                shape = Shapes.medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FilterDropdown(
                    label = "Category",
                    selectedValue = selectedCategory.ifBlank { "Choose" },
                    options = categoryOptions.ifEmpty { listOf("Other") },
                    onOptionSelected = onCategorySelected,
                    modifier = Modifier.weight(1f)
                )
                FilterDropdown(
                    label = "Account",
                    selectedValue = selectedAccount,
                    options = accountOptions,
                    onOptionSelected = onAccountSelected,
                    modifier = Modifier.weight(1f)
                )
            }
            FilterDropdown(
                label = "Currency",
                selectedValue = "${selectedCurrency.code} (${selectedCurrency.symbol})",
                options = Currency.entries.map { "${it.code} (${it.symbol})" },
                onOptionSelected = { selected ->
                    Currency.entries.firstOrNull { selected.startsWith(it.code) }?.let(onCurrencySelected)
                }
            )
            duplicateExpense?.let { duplicate ->
                Surface(
                    shape = Shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = "Possible duplicate detected",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${duplicate.category} on ${duplicate.date.format(DateTimeFormatter.ofPattern("dd MMM"))} using ${duplicate.paymentMethod} - ${formatCurrency(duplicate.amount, selectedCurrency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Button(
                onClick = onSave,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.medium
            ) {
                Text("Save Quick Expense")
            }
        }
    }
}

@Composable
private fun OnboardingCard(
    onQuickAdd: () -> Unit,
    onAddExpense: () -> Unit,
    onReportsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = "Start tracking in three quick steps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Use Quick Add for daily spending, add full details when needed, and check Reports after a few entries.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                OutlinedButton(onClick = onQuickAdd, modifier = Modifier.weight(1f), shape = Shapes.medium) {
                    Text("Quick Add")
                }
                Button(onClick = onAddExpense, modifier = Modifier.weight(1f), shape = Shapes.medium) {
                    Text("Full Entry")
                }
            }
            OutlinedButton(
                onClick = onReportsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.medium
            ) {
                Text("Open Reports")
            }
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.clickable { expanded = true },
        shape = Shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = selectedValue,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SortDropdown(
    options: List<TransactionSortOption>,
    selected: TransactionSortOption,
    onSelected: (TransactionSortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }, shape = Shapes.medium) {
        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text("Sort: ${selected.label}")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.label) },
                onClick = {
                    onSelected(option)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(148.dp)
            .clickable(onClick = onClick),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                text = title, 
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 32.dp)
            )
        }
    }
}

@Composable
private fun CategoryMiniCard(
    category: Category,
    amount: Double,
    currency: Currency = Currency.getDefault(),
    modifier: Modifier = Modifier
) {
    val color = categoryColor(category)
    Card(
        modifier = modifier,
        shape = Shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatCurrencyRounded(amount, currency),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Spent this month",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    categoryColor: Color,
    selected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val isTransfer = expense.isTransfer
    val isSplit = expense.splitGroupId != null
    val currency = Currency.fromCode(expense.currencyCode)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = onSelectionChange
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconCircle(
                    icon = when {
                        isTransfer -> Icons.AutoMirrored.Filled.ArrowForward
                        isSplit -> Icons.Default.Category
                        else -> Icons.Default.ShoppingCart
                    },
                    tint = when {
                        isTransfer -> Color(0xFF2196F3)
                        isSplit -> Color(0xFF9C27B0)
                        else -> categoryColor
                    },
                    size = 44.dp,
                    iconSize = 22.dp
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expense.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isTransfer) {
                            Surface(
                                shape = Shapes.full,
                                color = Color(0xFF2196F3).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "TRANSFER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2196F3),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                                )
                            }
                        }
                        if (isSplit) {
                            Surface(
                                shape = Shapes.full,
                                color = Color(0xFF9C27B0).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "SPLIT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF9C27B0),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                                )
                            }
                        }
                    }
                    if (expense.subcategory?.isNotBlank() == true) {
                        Text(
                            text = expense.subcategory,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (expense.description.isNotBlank()) {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isTransfer && expense.transferDestinationAccount != null) {
                        Text(
                            text = "To: ${expense.transferDestinationAccount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = expense.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = formatCurrency(expense.amount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isTransfer) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (expense.paymentMethod != "Cash") {
                        Surface(
                            shape = Shapes.full,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = expense.paymentMethod.uppercase(Locale.US),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    modifier: Modifier = Modifier,
    message: String
) {
    if (message.isBlank()) return
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun StatusMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = Shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Double.toEditableAmount(): String {
    if (this <= 0.0) return ""
    val integerValue = toLong()
    return if (this == integerValue.toDouble()) {
        integerValue.toString()
    } else {
        toString()
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    return runCatching {
        Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))
    }.getOrDefault("Unavailable")
}

private fun accountOptions(): List<String> {
    return listOf("All accounts", "Cash", "Bank", "UPI", "Credit Card", "Debit Card", "Wallet", "Other")
}

private fun sortComparator(option: TransactionSortOption): Comparator<Expense> {
    return when (option) {
        TransactionSortOption.NEWEST -> compareByDescending<Expense> { it.date }.thenByDescending { it.modifiedAt }
        TransactionSortOption.HIGHEST -> compareByDescending<Expense> { it.amount }.thenByDescending { it.date }
        TransactionSortOption.LOWEST -> compareBy<Expense> { it.amount }.thenByDescending { it.date }
        TransactionSortOption.CATEGORY -> compareBy<Expense> { it.category.lowercase(Locale.getDefault()) }.thenByDescending { it.date }
        TransactionSortOption.ACCOUNT -> compareBy<Expense> { it.paymentMethod.lowercase(Locale.getDefault()) }.thenByDescending { it.date }
    }
}

private fun buildMonthlySavingsSummaries(
    expenses: List<Expense>,
    incomeEntries: List<com.financetracker.data.model.IncomeEntry>,
    selectedMonth: YearMonth,
    selectedMonthIncome: Double
): List<MonthSavingsSummary> {
    val expenseMonths = expenses.map { YearMonth.from(it.date) }
    val incomeMonths = incomeEntries.mapNotNull { runCatching { YearMonth.parse(it.period) }.getOrNull() }
    val months = (expenseMonths + incomeMonths + selectedMonth).distinct().sorted()

    return months.map { month ->
        val income = incomeEntries.firstOrNull { it.period == month.toString() }?.amount
            ?: if (month == selectedMonth) selectedMonthIncome else 0.0
        val spending = expenses.filter { YearMonth.from(it.date) == month }.spendingTotal()
        MonthSavingsSummary(
            month = month,
            income = income,
            spending = spending
        )
    }
}

private fun upcomingRecurringEntries(
    entries: List<RecurringEntry>,
    selectedMonth: YearMonth
): List<RecurringEntry> {
    val today = LocalDate.now()
    val cutoffDay = if (YearMonth.from(today) == selectedMonth) today.dayOfMonth else 0
    return entries
        .filter { it.active && it.dayOfMonth > cutoffDay }
        .sortedWith(compareBy<RecurringEntry> { it.dayOfMonth }.thenBy { it.type.name })
}

private fun convertedSpending(
    expenses: List<Expense>,
    preferredCurrency: Currency,
    rates: Map<String, Double>,
    enabled: Boolean
): Double? {
    if (!enabled) return null
    var missingRate = false
    val total = expenses.filterNot { it.isTransfer }.sumOf { expense ->
        val expenseCurrency = Currency.fromCode(expense.currencyCode)
        when {
            expenseCurrency == preferredCurrency -> expense.amount
            rates[expenseCurrency.code] != null -> expense.amount * rates.getValue(expenseCurrency.code)
            else -> {
                missingRate = true
                0.0
            }
        }
    }
    return if (missingRate) null else total
}

private fun buildEffectiveBudgets(
    budgets: List<CategoryBudget>,
    expenses: List<Expense>,
    rolloverSettings: List<CategoryRolloverSetting>,
    selectedMonth: YearMonth
): Map<String, Double> {
    val settingsByCategory = rolloverSettings.associateBy { it.category.lowercase(Locale.getDefault()) }
    val selectedBudgets = budgets
        .filter { it.period == selectedMonth.toString() }
        .associateBy { it.category }
    return selectedBudgets.mapValues { (category, budget) ->
        val setting = settingsByCategory[category.lowercase(Locale.getDefault())]
        if (setting?.enabled != true) {
            budget.amount
        } else {
            val previousMonth = selectedMonth.minusMonths(1)
            val previousBudget = budgets.firstOrNull {
                it.period == previousMonth.toString() && it.category.equals(category, ignoreCase = true)
            }?.amount ?: 0.0
            val previousSpent = expenses
                .filter { YearMonth.from(it.date) == previousMonth && it.category.equals(category, ignoreCase = true) }
                .spendingTotal()
            val rollover = (previousBudget - previousSpent).let { amount ->
                if (amount >= 0.0 || setting.carryOverspend) amount else 0.0
            }
            budget.amount + rollover
        }
    }
}

private fun isExpenseOverEffectiveBudget(
    expense: Expense,
    visibleMonthExpenses: List<Expense>,
    effectiveBudgetsByCategory: Map<String, Double>
): Boolean {
    val effectiveBudget = effectiveBudgetsByCategory[expense.category] ?: return false
    if (effectiveBudget <= 0.0) return false
    val categorySpent = visibleMonthExpenses
        .filter { it.category == expense.category && Currency.fromCode(it.currencyCode) == Currency.fromCode(expense.currencyCode) }
        .spendingTotal()
    return categorySpent > effectiveBudget
}

private fun parseCurrentPeriodFromSheet(sheetName: String): YearMonth {
    val rawPeriod = sheetName.removePrefix("expenses_").replace("_", "-")
    return runCatching { YearMonth.parse(rawPeriod) }.getOrElse { YearMonth.now() }
}
