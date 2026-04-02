package com.financetracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financetracker.data.model.Category
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    val categoryState = uiState.categoryState
    val expenses = uiState.expenses
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { (_, exp) -> exp.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val totalExpenses = expenses.sumOf { it.amount }
    val highestSpend = categoryTotals.firstOrNull()?.second ?: 0.0
    val highestCategory = categoryTotals.firstOrNull()?.first ?: "None yet"
    val averageExpense = if (expenses.isNotEmpty()) totalExpenses / expenses.size else 0.0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reports",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (expenses.isEmpty()) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconCircle(
                    icon = Icons.Default.TrendingUp,
                    tint = MaterialTheme.colorScheme.primary,
                    size = 72.dp,
                    iconSize = 36.dp
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = "No data to analyze yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "Add some expenses and your reports will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding, vertical = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Summary Card
                ReportSummaryCard(
                    totalExpenses = totalExpenses,
                    transactionCount = expenses.size,
                    highestCategory = highestCategory,
                    averageExpense = averageExpense
                )

                // Category Breakdown
                SectionHeader(
                    title = "Category Breakdown",
                    subtitle = "See where your money is going"
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        // Donut Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(
                                data = categoryTotals,
                                total = totalExpenses,
                                categories = categoryState.categories
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrencyRounded(totalExpenses),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        // Category List
                        categoryTotals.forEach { (category, amount) ->
                            val percentage = if (totalExpenses > 0) amount / totalExpenses * 100 else 0.0
                            val accent = categoryColor(categoryState.categories.find { it.name == category })

                            Column(modifier = Modifier.padding(vertical = Spacing.sm)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(accent)
                                        )
                                        Text(
                                            category,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            formatCurrency(amount),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = String.format(Locale.US, "%.0f%%", percentage),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = accent
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                LinearProgressIndicator(
                                    progress = (amount / totalExpenses).toFloat(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(Shapes.full),
                                    color = accent,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }

                // Budget Progress
                if (categoryTotals.any { (category, _) ->
                        categoryState.categories.find { it.name == category }?.monthlyBudget?.let { it > 0 } == true
                    }) {
                    SectionHeader(
                        title = "Budget Progress",
                        subtitle = "Track your spending against budgets"
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
                    ) {
                        Column(modifier = Modifier.padding(Spacing.lg)) {
                            categoryTotals.forEach { (category, amount) ->
                                val categoryModel = categoryState.categories.find { it.name == category }
                                val budget = categoryModel?.monthlyBudget

                                if (budget != null && budget > 0.0) {
                                    val accent = categoryColor(categoryModel)
                                    val progress = (amount / budget).toFloat().coerceIn(0f, 1f)
                                    val isOverBudget = amount > budget

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = Spacing.sm)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    category,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${formatCurrency(amount)} of ${formatCurrency(budget)}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = if (isOverBudget) "Over!" else "${(progress * 100).toInt()}%",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isOverBudget) MaterialTheme.colorScheme.error else accent
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(Spacing.xs))
                                        LinearProgressIndicator(
                                            progress = progress,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(Shapes.full),
                                            color = if (isOverBudget) MaterialTheme.colorScheme.error else accent,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(
    totalExpenses: Double,
    transactionCount: Int,
    highestCategory: String,
    averageExpense: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Expenses",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalExpenses),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconCircle(
                    icon = Icons.Default.TrendingDown,
                    tint = MaterialTheme.colorScheme.primary,
                    size = 48.dp,
                    iconSize = 24.dp
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                SummaryStat(
                    label = "Transactions",
                    value = transactionCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryStat(
                    label = "Average",
                    value = formatCurrencyRounded(averageExpense),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Top: ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = highestCategory,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = Shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label.uppercase(Locale.US),
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DonutChart(
    data: List<Pair<String, Double>>,
    total: Double,
    categories: List<Category>
) {
    if (data.isEmpty() || total <= 0.0) return

    Canvas(modifier = Modifier.size(160.dp)) {
        val diameter = size.minDimension
        val strokeWidth = diameter * 0.2f
        val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        var startAngle = -90f

        data.forEach { (category, amount) ->
            val sweepAngle = ((amount / total) * 360f).toFloat()
            val accent = categoryColor(categories.find { it.name == category })

            drawArc(
                color = accent,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle + 4f
        }
    }
}

@Composable
private fun CategoryLegendItem(
    category: String,
    amount: Double,
    percentage: Double,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(accent)
            )
            Column {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatCurrency(amount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = accent.copy(alpha = 0.12f)
        ) {
            Text(
                text = String.format(Locale.US, "%.1f%%", percentage),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun BudgetProgressItem(
    category: String,
    amount: Double,
    budget: Double?,
    accent: Color
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatCurrency(amount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (budget != null && budget > 0.0) {
                val percentage = ((amount / budget) * 100).toInt()
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (percentage > 100) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    } else {
                        accent.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (percentage > 100) {
                            MaterialTheme.colorScheme.error
                        } else {
                            accent
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            } else {
                Text(
                    text = "No budget",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (budget != null && budget > 0.0) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = (amount / budget).toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = if (amount > budget) MaterialTheme.colorScheme.error else accent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Budget ${formatCurrencyRounded(budget)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
