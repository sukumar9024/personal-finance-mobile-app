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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financetracker.data.model.Category
import com.financetracker.data.model.Expense
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
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val averageSpend = if (uiState.expenses.isNotEmpty()) {
        uiState.totalAmount / uiState.expenses.size
    } else {
        0.0
    }
    val highlightedCategories = uiState.categoryState.categories.take(4)
    val currentMonthText = uiState.currentMonthSheet
        .removePrefix("expenses_")
        .replace("_", " / ")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    totalAmount = uiState.totalAmount,
                    transactionCount = uiState.expenses.size,
                    averageSpend = averageSpend
                )
            }

            item {
                QuickActionsRow(
                    onReportsClick = onReportsClick,
                    onCategoriesClick = onCategoriesClick,
                    onSettingsClick = onSettingsClick
                )
            }

            if (uiState.errorMessage != null) {
                item {
                    ErrorBanner(message = uiState.errorMessage)
                }
            }

            if (highlightedCategories.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Top Categories",
                        subtitle = "Where your money goes",
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
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Recent Transactions",
                    subtitle = "Your latest spending activity"
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

            if (uiState.expenses.isEmpty() && !uiState.isLoading) {
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
                                text = "No expenses yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "Tap the + button to add your first expense and start tracking",
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

            if (uiState.expenses.isNotEmpty()) {
                items(uiState.expenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        categoryColor = categoryColor(
                            uiState.categoryState.categories.find { it.name == expense.category }
                        ),
                        onClick = { onExpenseClick(expense) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(
    totalAmount: Double,
    transactionCount: Int,
    averageSpend: Double
) {
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
                            text = "Total Spending",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(Spacing.xxs))
                        Text(
                            text = formatCurrency(totalAmount),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    BadgeChip(
                        text = "$transactionCount transactions",
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
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
                                text = "Average",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatCurrency(averageSpend),
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
                                text = "Per Txn",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatCurrencyRounded(averageSpend),
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
            subtitle = "Analytics",
            icon = Icons.Default.Analytics,
            onClick = onReportsClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            title = "Categories",
            subtitle = "Manage",
            icon = Icons.Default.Category,
            onClick = onCategoriesClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            title = "Settings",
            subtitle = "Setup",
            icon = Icons.Default.Settings,
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f)
        )
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
        modifier = modifier.clickable(onClick = onClick),
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
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryMiniCard(
    category: Category,
    amount: Double,
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
                text = formatCurrencyRounded(amount),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    categoryColor: Color,
    onClick: () -> Unit
) {
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
                    icon = Icons.Default.ShoppingCart,
                    tint = categoryColor,
                    size = 44.dp,
                    iconSize = 22.dp
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
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
                    text = formatCurrency(expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
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