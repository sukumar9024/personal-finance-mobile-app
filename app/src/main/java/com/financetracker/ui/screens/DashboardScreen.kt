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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.financetracker.data.model.Category
import com.financetracker.data.model.Currency
import com.financetracker.data.model.Expense
import com.financetracker.data.model.TransactionType
import com.financetracker.data.model.isTransfer
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

private enum class TransactionSortOption(val label: String) {
    NEWEST("Newest"),
    HIGHEST("Highest"),
    LOWEST("Lowest"),
    CATEGORY("Category"),
    ACCOUNT("Account")
}

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
    var budgetInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All categories") }
    var selectedAccountFilter by remember { mutableStateOf("All accounts") }
    var selectedSort by remember { mutableStateOf(TransactionSortOption.NEWEST) }
    var quickAddAmount by remember { mutableStateOf("") }
    var quickAddCategory by remember { mutableStateOf("") }
    var quickAddAccount by remember { mutableStateOf("Cash") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val averageSpend = if (uiState.expenses.isNotEmpty()) {
        uiState.totalAmount / uiState.expenses.size
    } else {
        0.0
    }
    val spendByCategory = uiState.expenses
        .groupBy { it.category }
        .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    val categoryBudgetsByCategory = uiState.categoryBudgets
        .filter { it.period == parseCurrentPeriodFromSheet(uiState.currentMonthSheet).toString() }
        .associateBy { it.category }
    val highlightedCategories = uiState.categoryState.categories
        .sortedWith(
            compareByDescending<Category> { spendByCategory[it.name] ?: 0.0 }
                .thenByDescending { categoryBudgetsByCategory[it.name]?.amount ?: 0.0 }
                .thenBy { it.name }
        )
        .take(4)
    val currentMonthText = uiState.currentMonthSheet
        .removePrefix("expenses_")
        .replace("_", " / ")
    val errorMessage = uiState.errorMessage
    val remainingAmount = uiState.monthlyIncome - uiState.totalAmount
    val parsedBudget = budgetInput.toDoubleOrNull()
    val canSaveBudget = parsedBudget != null && parsedBudget != uiState.monthlyIncome
    val currency = uiState.currency

    LaunchedEffect(uiState.monthlyIncome) {
        budgetInput = uiState.monthlyIncome.toEditableAmount()
    }

    val categoryFilterOptions = listOf("All categories") + uiState.categoryState.categories.map { it.name }
    val accountOptions = accountOptions()
    if (quickAddCategory.isBlank() && uiState.categoryState.categories.isNotEmpty()) {
        quickAddCategory = uiState.categoryState.categories.first().name
    }
    val filteredExpenses = uiState.expenses.filter { expense ->
        val matchesQuery = searchQuery.isBlank() || listOf(
            expense.category,
            expense.subcategory.orEmpty(),
            expense.description,
            expense.amount.toString()
        ).any { it.contains(searchQuery, ignoreCase = true) }
        val matchesCategory = selectedCategoryFilter == "All categories" || expense.category == selectedCategoryFilter
        val matchesAccount = selectedAccountFilter == "All accounts" || expense.paymentMethod == selectedAccountFilter
        matchesQuery && matchesCategory && matchesAccount
    }.sortedWith(sortComparator(selectedSort))
    val quickAddDuplicate = quickAddAmount.toDoubleOrNull()?.let { amount ->
        uiState.expenses.firstOrNull { expense ->
            expense.date == LocalDate.now() &&
                expense.category == quickAddCategory &&
                expense.paymentMethod == quickAddAccount &&
                kotlin.math.abs(expense.amount - amount) < 0.001
        }
    }
    val lastSyncedText = uiState.syncStatus.lastSuccessfulSyncMillis?.let(::formatTimestamp)
        ?: "No successful sync yet"
    val lastAttemptText = uiState.syncStatus.lastSyncAttemptMillis?.let(::formatTimestamp)
        ?: "No refresh attempts yet"
    val refreshLabel = if (uiState.syncStatus.lastSyncError != null) "Retry Sync" else "Refresh Data"

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
                bottom = paddingValues.calculateBottomPadding() + 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item {
                BalanceCard(
                    monthlyIncome = uiState.monthlyIncome,
                    totalAmount = uiState.totalAmount,
                    remainingAmount = remainingAmount,
                    transactionCount = uiState.expenses.size,
                    averageSpend = averageSpend,
                    currency = currency
                )
            }

            item {
                DashboardControlCard(
                    currentMonthText = currentMonthText,
                    budgetInput = budgetInput,
                    onBudgetInputChange = { value ->
                        budgetInput = value.filter { it.isDigit() || it == '.' }
                    },
                    onSaveBudget = {
                        parsedBudget?.let(viewModel::setMonthlyIncome)
                    },
                    canSaveBudget = canSaveBudget && !uiState.isLoading,
                    entryCount = uiState.expenses.size,
                    categoryCount = uiState.categoryState.categories.size,
                    isLoading = uiState.isLoading,
                    onRefreshClick = viewModel::refreshData,
                    syncStatus = uiState.syncStatus,
                    lastSyncedText = lastSyncedText,
                    lastAttemptText = lastAttemptText,
                    refreshLabel = refreshLabel
                )
            }

            item {
                QuickAddCard(
                    amount = quickAddAmount,
                    onAmountChange = { quickAddAmount = it.filter { char -> char.isDigit() || char == '.' } },
                    categoryOptions = uiState.categoryState.categories.map { it.name },
                    selectedCategory = quickAddCategory,
                    onCategorySelected = { quickAddCategory = it },
                    accountOptions = accountOptions.drop(1),
                    selectedAccount = quickAddAccount,
                    onAccountSelected = { quickAddAccount = it },
                    duplicateExpense = quickAddDuplicate,
                    currency = currency,
                    onSave = {
                        quickAddAmount.toDoubleOrNull()?.let { amount ->
                            val expense = Expense(
                                date = LocalDate.now(),
                                amount = amount,
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

            if (errorMessage != null) {
                item {
                    ErrorBanner(message = errorMessage)
                }
            }

            item {
                QuickActionsRow(
                    onReportsClick = onReportsClick,
                    onCategoriesClick = onCategoriesClick,
                    onSettingsClick = onSettingsClick
                )
            }

            if (highlightedCategories.isNotEmpty()) {
                item {
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
                }

                item {
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
                TransactionFilterCard(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    categoryOptions = categoryFilterOptions,
                    selectedCategory = selectedCategoryFilter,
                    onCategorySelected = { selectedCategoryFilter = it },
                    accountOptions = accountOptions,
                    selectedAccount = selectedAccountFilter,
                    onAccountSelected = { selectedAccountFilter = it },
                    sortOptions = TransactionSortOption.entries,
                    selectedSort = selectedSort,
                    onSortSelected = { selectedSort = it }
                )
            }

            item {
                SectionHeader(
                    title = "Recent Transactions",
                    subtitle = "${filteredExpenses.size} matching transactions this month"
                )
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

            if (filteredExpenses.isEmpty() && !uiState.isLoading) {
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
                                icon = Icons.Default.ReceiptLong,
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

            if (filteredExpenses.isNotEmpty()) {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        categoryColor = categoryColor(
                            uiState.categoryState.categories.find { it.name == expense.category }
                        ),
                        currency = currency,
                        onClick = { onExpenseClick(expense) }
                    )
                }
            }
        }
    }
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
                            text = "Monthly Overview",
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
                                "Budget set for this month"
                            } else {
                                "Set this month's budget below"
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
    budgetInput: String,
    onBudgetInputChange: (String) -> Unit,
    onSaveBudget: () -> Unit,
    canSaveBudget: Boolean,
    entryCount: Int,
    categoryCount: Int,
    isLoading: Boolean,
    onRefreshClick: () -> Unit,
    syncStatus: com.financetracker.ui.viewmodel.SyncStatus,
    lastSyncedText: String,
    lastAttemptText: String,
    refreshLabel: String
) {
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
                title = "Budget & Sync",
                subtitle = "Keep this month updated and editable from the home screen"
            )

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
                value = budgetInput,
                onValueChange = onBudgetInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly budget") },
                prefix = { Text("Rs") },
                singleLine = true,
                shape = Shapes.medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Button(
                    onClick = onSaveBudget,
                    enabled = canSaveBudget,
                    modifier = Modifier.weight(1f),
                    shape = Shapes.medium
                ) {
                    Text("Save Budget")
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
        }
    }
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
                subtitle = "Find transactions by text, category, or payment method"
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
    duplicateExpense: Expense?,
    currency: Currency = Currency.getDefault(),
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
                prefix = { Text("Rs") },
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
                            text = "${duplicate.category} on ${duplicate.date.format(DateTimeFormatter.ofPattern("dd MMM"))} using ${duplicate.paymentMethod} - ${formatCurrency(duplicate.amount, currency)}",
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
        Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
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
    currency: Currency = Currency.getDefault(),
    onClick: () -> Unit
) {
    val isTransfer = expense.isTransfer
    val isSplit = expense.splitGroupId != null
    
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
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconCircle(
                    icon = when {
                        isTransfer -> Icons.Default.ArrowForward
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
                        imageVector = Icons.Default.ArrowForward,
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

private fun parseCurrentPeriodFromSheet(sheetName: String): YearMonth {
    val rawPeriod = sheetName.removePrefix("expenses_").replace("_", "-")
    return runCatching { YearMonth.parse(rawPeriod) }.getOrElse { YearMonth.now() }
}
