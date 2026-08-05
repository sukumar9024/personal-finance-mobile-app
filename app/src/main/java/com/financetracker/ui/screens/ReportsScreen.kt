package com.financetracker.ui.screens
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financetracker.data.model.Category
import com.financetracker.data.model.Expense
import com.financetracker.data.model.IncomeEntry
import com.financetracker.data.model.isTransfer
import com.financetracker.ui.theme.CardElevation
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Store
import com.financetracker.ui.theme.CategoryColors
import com.financetracker.data.model.CategoryRolloverSetting
import com.financetracker.data.model.Currency
import com.financetracker.ui.theme.IconCircle
import com.financetracker.data.model.MonthlyCloseNote
import com.financetracker.ui.theme.ScreenPadding
import com.financetracker.ui.theme.SectionHeader
import com.financetracker.ui.theme.Shapes
import com.financetracker.ui.theme.Spacing
import com.financetracker.ui.theme.categoryColor
import com.financetracker.ui.theme.formatCurrency
import com.financetracker.ui.theme.formatCurrencyRounded
import com.financetracker.ui.viewmodel.ExpenseViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.text.style.TextOverflow

private enum class TimelineFilter(val label: String) {
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

private data class TrendPoint(
    val label: String,
    val income: Double,
    val spending: Double,
    val remaining: Double
)

private data class SpendingSlice(
    val label: String,
    val amount: Double,
    val color: Color
)

private data class CurrencyReportSummary(
    val currency: Currency,
    val amount: Double,
    val transactionCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = uiState.categoryState.categories
    val currentPeriod = parseCurrentPeriod(uiState.currentMonthSheet)
    var selectedTimeline by rememberSaveable { mutableStateOf(TimelineFilter.MONTHLY) }
    var selectedCategory by rememberSaveable { mutableStateOf("All categories") }
    var editingIncomePeriod by rememberSaveable { mutableStateOf(currentPeriod.toString()) }
    var editingIncomeAmount by rememberSaveable { mutableStateOf("") }
    var showIncomeDialog by rememberSaveable { mutableStateOf(false) }

    val categoryOptions = listOf("All categories") + categories.map { it.name }
    if (selectedCategory !in categoryOptions) selectedCategory = "All categories"

    val reportExpenses = if (uiState.includeTransfersInReports) {
        uiState.reportExpenses
    } else {
        uiState.reportExpenses.filterNot { it.isTransfer }
    }
    val currentMonthExpenses = if (uiState.includeTransfersInReports) {
        uiState.expenses
    } else {
        uiState.expenses.filterNot { it.isTransfer }
    }
    val scopedExpenses = filterExpensesForTimeline(
        expenses = reportExpenses,
        timeline = selectedTimeline,
        currentPeriod = currentPeriod
    )
    val filteredExpenses = if (selectedCategory == "All categories") {
        scopedExpenses
    } else {
        scopedExpenses.filter { it.category == selectedCategory }
    }
    val primaryFilteredExpenses = filteredExpenses.filter { Currency.fromCode(it.currencyCode) == uiState.currency }
    val currencyReportSummaries = filteredExpenses
        .groupBy { Currency.fromCode(it.currencyCode) }
        .map { (currency, entries) -> CurrencyReportSummary(currency, entries.sumOf { it.amount }, entries.size) }
        .sortedByDescending { it.amount }

    val trendPoints = buildTrendPoints(
        timeline = selectedTimeline,
        expenses = primaryFilteredExpenses,
        incomeEntries = uiState.incomeEntries,
        currentPeriod = currentPeriod,
        currentMonthlyIncome = uiState.monthlyIncome
    )
    val categoryTotals = primaryFilteredExpenses
        .groupBy { it.category }
        .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
    val totalIncome = trendPoints.sumOf { it.income }
    val totalSpending = primaryFilteredExpenses.sumOf { it.amount }
    val remainingAmount = totalIncome - totalSpending
    val averageExpense = if (primaryFilteredExpenses.isNotEmpty()) totalSpending / primaryFilteredExpenses.size else 0.0
    val spendingSlices = buildSpendingSlices(categoryTotals, categories, totalIncome, remainingAmount)
    val monthComparison = buildMonthComparison(uiState.reportExpenses, uiState.incomeEntries, currentPeriod)
    val incomeHistory = uiState.incomeEntries.sortedByDescending { it.period }.take(12)
    val spendingInsights = buildSpendingInsights(
        expenses = reportExpenses,
        incomeEntries = uiState.incomeEntries,
        categoryBudgets = uiState.categoryBudgets,
        currentPeriod = currentPeriod,
        currentMonthlyIncome = uiState.monthlyIncome
    )
    val monthlyCloseNote = uiState.monthlyCloseNotes.firstOrNull { it.period == currentPeriod.toString() }
        ?: MonthlyCloseNote(currentPeriod.toString())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Reports", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            IncomeVsSpendingCard(
                totalIncome = totalIncome,
                totalSpending = totalSpending,
                remainingAmount = remainingAmount,
                averageExpense = averageExpense,
                transactionCount = filteredExpenses.size,
                spendingSlices = spendingSlices
            )

            MultiCurrencyReportCard(currencyReportSummaries)

            BudgetTrendCard(
                selectedTimeline = selectedTimeline,
                onTimelineSelected = { selectedTimeline = it },
                categoryOptions = categoryOptions,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                points = trendPoints
            )

            CategoryBreakdownCard(
                categoryTotals = categoryTotals,
                totalSpending = totalSpending,
                categories = categories
            )

            BudgetVsActualCard(
                categories = categories,
                expenses = currentMonthExpenses,
                monthlyIncome = uiState.monthlyIncome,
                categoryBudgets = uiState.categoryBudgets,
                currentPeriod = currentPeriod,
                rolloverSettings = uiState.categoryRolloverSettings,
                allExpenses = reportExpenses,
                onRolloverChange = viewModel::setCategoryRollover
            )

            MonthComparisonCard(
                currentPeriod = currentPeriod,
                comparison = monthComparison
            )

            SpendingInsightsCard(spendingInsights)

            IncomeHistoryCard(
                incomeEntries = incomeHistory,
                onEditEntry = { entry ->
                    editingIncomePeriod = entry.period
                    editingIncomeAmount = entry.amount.toEditableAmount()
                    showIncomeDialog = true
                },
                onAddEntry = {
                    editingIncomePeriod = currentPeriod.toString()
                    editingIncomeAmount = uiState.monthlyIncome.toEditableAmount()
                    showIncomeDialog = true
                }
            )

            MonthEndForecastCard(
                currentPeriod = currentPeriod,
                monthlyIncome = uiState.monthlyIncome,
                totalSpending = uiState.expenses.filter { !it.isTransfer }.sumOf { it.amount },
                remainingDays = currentPeriod.atEndOfMonth().dayOfMonth - LocalDate.now().dayOfMonth
            )

            MonthlyCloseReviewCard(
                currentPeriod = currentPeriod,
                monthlyIncome = uiState.monthlyIncome,
                expenses = currentMonthExpenses,
                note = monthlyCloseNote,
                onSaveNote = { viewModel.saveMonthlyCloseNote(currentPeriod.toString(), it) },
                onExport = viewModel::exportData
            )

            TopMerchantsCard(
                expenses = scopedExpenses.filter { !it.isTransfer }
            )
        }
    }

    if (showIncomeDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showIncomeDialog = false },
            title = { Text("Edit Income History") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedTextField(
                        value = editingIncomePeriod,
                        onValueChange = { editingIncomePeriod = it },
                        label = { Text("Month (YYYY-MM)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editingIncomeAmount,
                        onValueChange = { value ->
                            editingIncomeAmount = value.filter { it.isDigit() || it == '.' }
                        },
                        label = { Text("Income amount") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingIncomeAmount.toDoubleOrNull()?.let {
                            viewModel.setMonthlyIncomeForPeriod(editingIncomePeriod, it)
                        }
                        showIncomeDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncomeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}

@Composable
private fun IncomeVsSpendingCard(
    totalIncome: Double,
    totalSpending: Double,
    remainingAmount: Double,
    averageExpense: Double,
    transactionCount: Int,
    spendingSlices: List<SpendingSlice>
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Income vs Spending", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = formatCurrency(remainingAmount),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingAmount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = if (remainingAmount >= 0) "Remaining after spending" else "Spending is above monthly income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconCircle(
                    icon = if (remainingAmount >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    tint = if (remainingAmount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    size = 52.dp,
                    iconSize = 24.dp
                )
            }

            SpendingCompositionBar(totalIncome = totalIncome, spendingSlices = spendingSlices)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                MetricCard("Income", formatCurrencyRounded(totalIncome), Modifier.weight(1f))
                MetricCard("Spent", formatCurrencyRounded(totalSpending), Modifier.weight(1f))
                MetricCard("Avg / Txn", formatCurrencyRounded(averageExpense), Modifier.weight(1f))
            }

            Text(
                text = "$transactionCount transactions in this view",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MultiCurrencyReportCard(
    summaries: List<CurrencyReportSummary>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Currency Totals",
                subtitle = "Spending is grouped by the currency used on each transaction"
            )
            if (summaries.isEmpty()) {
                Text("No spending found for the selected report filters.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                summaries.forEach { summary ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(summary.currency.displayName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text("${summary.transactionCount} transactions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatCurrency(summary.amount, summary.currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpendingCompositionBar(
    totalIncome: Double,
    spendingSlices: List<SpendingSlice>
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(Shapes.full)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
        ) {
            if (totalIncome > 0.0 && spendingSlices.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    spendingSlices.forEach { slice ->
                        Box(
                            modifier = Modifier
                                .weight((slice.amount / totalIncome).toFloat().coerceAtLeast(0.0001f))
                                .fillMaxSize()
                                .background(slice.color)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            spendingSlices.forEach { slice ->
                LegendChip(label = slice.label, color = slice.color)
                Spacer(modifier = Modifier.width(Spacing.xs))
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = Shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
    ) {
        Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Text(label.uppercase(Locale.US), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BudgetTrendCard(
    selectedTimeline: TimelineFilter,
    onTimelineSelected: (TimelineFilter) -> Unit,
    categoryOptions: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    points: List<TrendPoint>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Budget Trend",
                subtitle = "Monthly or yearly view of income, spending, and remaining balance"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                TrendDropdown(
                    label = "Timeline",
                    selectedValue = selectedTimeline.label,
                    options = TimelineFilter.entries.map { it.label },
                    onOptionSelected = { value ->
                        TimelineFilter.entries.firstOrNull { it.label == value }?.let(onTimelineSelected)
                    },
                    modifier = Modifier.weight(1f)
                )
                TrendDropdown(
                    label = "Category",
                    selectedValue = selectedCategory,
                    options = categoryOptions,
                    onOptionSelected = onCategorySelected,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                LegendChip("Income", Color(0xFF2563EB))
                LegendChip("Spending", Color(0xFFF97316))
                LegendChip("Remaining", Color(0xFF16A34A))
            }

            if (points.isEmpty()) {
                Text("No trend data is available for the selected filters.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                TrendLineChart(points)
            }
        }
    }
}

@Composable
private fun TrendDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = Shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(selectedValue, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

@Composable
private fun LegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(99.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TrendLineChart(points: List<TrendPoint>) {
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    val incomeColor = Color(0xFF2563EB)
    val spendingColor = Color(0xFFF97316)
    val remainingColor = Color(0xFF16A34A)

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            val leftPadding = 18.dp.toPx()
            val rightPadding = 12.dp.toPx()
            val topPadding = 12.dp.toPx()
            val bottomPadding = 18.dp.toPx()
            val chartWidth = size.width - leftPadding - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding
            val maxValue = max(points.maxOf { max(max(it.income, it.spending), it.remaining) }, 1.0)
            val minValue = min(points.minOf { min(min(it.income, it.spending), it.remaining) }, 0.0)
            val range = (maxValue - minValue).takeIf { it > 0 } ?: 1.0

            repeat(4) { index ->
                val y = topPadding + (chartHeight / 3f) * index
                drawLine(axisColor, Offset(leftPadding, y), Offset(size.width - rightPadding, y), 1.dp.toPx())
            }

            fun point(index: Int, value: Double): Offset {
                val x = if (points.size == 1) leftPadding + chartWidth / 2f else leftPadding + chartWidth * (index / points.lastIndex.toFloat())
                val normalized = ((value - minValue) / range).toFloat()
                val y = size.height - bottomPadding - (normalized * chartHeight)
                return Offset(x, y)
            }

            fun drawSeries(values: List<Double>, color: Color) {
                val path = Path()
                values.forEachIndexed { index, value ->
                    val point = point(index, value)
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx()))
                values.forEachIndexed { index, value -> drawCircle(color = color, radius = 4.dp.toPx(), center = point(index, value)) }
            }

            drawSeries(points.map { it.income }, incomeColor)
            drawSeries(points.map { it.spending }, spendingColor)
            drawSeries(points.map { it.remaining }, remainingColor)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            points.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    categoryTotals: List<Pair<String, Double>>,
    totalSpending: Double,
    categories: List<Category>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(title = "Category Breakdown", subtitle = "Track spending distribution for the current report view")
            if (categoryTotals.isEmpty()) {
                Text("No expenses match the selected filters yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                categoryTotals.forEach { (category, amount) ->
                    val accent = categoryColor(categories.find { it.name == category })
                    val percentage = if (totalSpending > 0) (amount / totalSpending) * 100 else 0.0
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(99.dp)).background(accent))
                                Text(category, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatCurrency(amount), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                Text(String.format(Locale.US, "%.0f%%", percentage), style = MaterialTheme.typography.labelSmall, color = accent)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(Shapes.full)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((amount / totalSpending).toFloat().coerceIn(0f, 1f))
                                    .height(8.dp)
                                    .clip(Shapes.full)
                                    .background(accent)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildSpendingSlices(
    categoryTotals: List<Pair<String, Double>>,
    categories: List<Category>,
    totalIncome: Double,
    remainingAmount: Double
): List<SpendingSlice> {
    val slices = categoryTotals.map { (name, amount) ->
        SpendingSlice(
            label = "$name ${formatCurrencyRounded(amount)}",
            amount = amount,
            color = categoryColor(categories.find { it.name == name })
        )
    }.toMutableList()

    if (remainingAmount > 0) {
        slices += SpendingSlice(
            label = "Remaining ${formatCurrencyRounded(remainingAmount)}",
            amount = remainingAmount,
            color = Color(0xFF16A34A)
        )
    } else if (totalIncome <= 0.0) {
        slices += SpendingSlice(
            label = "Set monthly income",
            amount = 1.0,
            color = MaterialThemeFallbackColor
        )
    }

    return slices
}

private val MaterialThemeFallbackColor = Color(0xFF94A3B8)

private fun parseCurrentPeriod(sheetName: String): YearMonth {
    val rawPeriod = sheetName.removePrefix("expenses_").replace("_", "-")
    return runCatching { YearMonth.parse(rawPeriod) }.getOrElse { YearMonth.now() }
}

private fun filterExpensesForTimeline(
    expenses: List<Expense>,
    timeline: TimelineFilter,
    currentPeriod: YearMonth
): List<Expense> {
    return when (timeline) {
        TimelineFilter.MONTHLY -> expenses.filter { it.date.year == currentPeriod.year }
        TimelineFilter.YEARLY -> expenses
    }
}

private fun buildTrendPoints(
    timeline: TimelineFilter,
    expenses: List<Expense>,
    incomeEntries: List<IncomeEntry>,
    currentPeriod: YearMonth,
    currentMonthlyIncome: Double
): List<TrendPoint> {
    return when (timeline) {
        TimelineFilter.MONTHLY -> buildMonthlyTrendPoints(expenses, incomeEntries, currentPeriod, currentMonthlyIncome)
        TimelineFilter.YEARLY -> buildYearlyTrendPoints(expenses, incomeEntries)
    }.takeLast(7)
}

private fun buildMonthlyTrendPoints(
    expenses: List<Expense>,
    incomeEntries: List<IncomeEntry>,
    currentPeriod: YearMonth,
    currentMonthlyIncome: Double
): List<TrendPoint> {
    val expenseMonths = expenses.map { YearMonth.from(it.date) }
    val incomeMonths = incomeEntries.mapNotNull { runCatching { YearMonth.parse(it.period) }.getOrNull() }
    val visibleMonths = (expenseMonths + incomeMonths + currentPeriod).distinct().sorted().takeLast(6)

    return visibleMonths.map { month ->
        val income = incomeEntries.firstOrNull { it.period == month.toString() }?.amount ?: if (month == currentPeriod) currentMonthlyIncome else 0.0
        val spending = expenses.filter { YearMonth.from(it.date) == month }.sumOf { it.amount }
        TrendPoint(month.format(DateTimeFormatter.ofPattern("MMM")), income, spending, income - spending)
    }
}

private fun buildYearlyTrendPoints(expenses: List<Expense>, incomeEntries: List<IncomeEntry>): List<TrendPoint> {
    val expenseYears = expenses.map { it.date.year }
    val incomeYears = incomeEntries.mapNotNull { runCatching { YearMonth.parse(it.period).year }.getOrNull() }
    val visibleYears = (expenseYears + incomeYears).distinct().sorted().takeLast(4)

    return visibleYears.map { year ->
        val income = incomeEntries.mapNotNull { entry ->
            runCatching { YearMonth.parse(entry.period) }.getOrNull()?.takeIf { it.year == year }?.let { entry.amount }
        }.sum()
        val spending = expenses.filter { it.date.year == year }.sumOf { it.amount }
        TrendPoint(year.toString(), income, spending, income - spending)
    }
}

@Composable
private fun BudgetVsActualCard(
    categories: List<Category>,
    expenses: List<Expense>,
    monthlyIncome: Double,
    categoryBudgets: List<com.financetracker.data.model.CategoryBudget> = emptyList(),
    currentPeriod: YearMonth,
    rolloverSettings: List<CategoryRolloverSetting>,
    allExpenses: List<Expense>,
    onRolloverChange: (String, Boolean, Boolean) -> Unit
) {
    val currentPeriodText = currentPeriod.toString()
    val budgetsByCategory = categoryBudgets
        .filter { it.period == currentPeriodText }
        .associateBy { it.category }
    val effectiveBudgetsByCategory = buildEffectiveBudgetsForReports(
        budgets = categoryBudgets,
        expenses = allExpenses,
        rolloverSettings = rolloverSettings,
        selectedMonth = currentPeriod
    )
    val rolloverByCategory = rolloverSettings.associateBy { it.category.lowercase(Locale.getDefault()) }
    val spendByCategory = expenses.groupBy { it.category }.mapValues { (_, entries) -> entries.sumOf { it.amount } }
    val trackedCategories = categories.filter { 
        (budgetsByCategory[it.name]?.amount ?: 0.0) > 0.0 || (spendByCategory[it.name] ?: 0.0) > 0.0 
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Budget vs Actual",
                subtitle = if (monthlyIncome > 0.0) "See which categories are under or over plan" else "Set category budgets to track plan versus actual"
            )

            if (trackedCategories.isEmpty()) {
                Text(
                    text = "No category budgets are configured yet. Tap a category in the Categories screen to set one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                trackedCategories.forEach { category ->
                    val spent = spendByCategory[category.name] ?: 0.0
                    val budget = budgetsByCategory[category.name]?.amount ?: 0.0
                    val effectiveBudget = effectiveBudgetsByCategory[category.name] ?: budget
                    val progress = if (effectiveBudget > 0.0) (spent / effectiveBudget).toFloat().coerceIn(0f, 1f) else 0f
                    val accent = categoryColor(category)
                    val rolloverSetting = rolloverByCategory[category.name.lowercase(Locale.getDefault())]

                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${formatCurrency(spent)} / ${if (effectiveBudget > 0.0) formatCurrency(effectiveBudget) else "No budget"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (effectiveBudget > 0.0 && spent > effectiveBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(Shapes.full)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(8.dp)
                                    .clip(Shapes.full)
                                    .background(accent)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rollover unused budget",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            androidx.compose.material3.Switch(
                                checked = rolloverSetting?.enabled == true,
                                onCheckedChange = {
                                    onRolloverChange(category.name, it, rolloverSetting?.carryOverspend ?: true)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthComparisonCard(
    currentPeriod: YearMonth,
    comparison: MonthComparison
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Month Comparison",
                subtitle = "${currentPeriod.minusMonths(1).format(DateTimeFormatter.ofPattern("MMM yyyy"))} vs ${currentPeriod.format(DateTimeFormatter.ofPattern("MMM yyyy"))}"
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ComparisonMetric("Income", comparison.currentIncome, comparison.previousIncome, Modifier.weight(1f))
                ComparisonMetric("Spent", comparison.currentSpent, comparison.previousSpent, Modifier.weight(1f))
                ComparisonMetric("Remaining", comparison.currentRemaining, comparison.previousRemaining, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SpendingInsightsCard(insights: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Spending Insights",
                subtitle = "Plain-language changes detected from your history"
            )
            if (insights.isEmpty()) {
                Text("Add more monthly history to unlock richer insights.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                insights.forEach { insight ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = Shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f)
                    ) {
                        Text(insight, modifier = Modifier.padding(Spacing.md), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyCloseReviewCard(
    currentPeriod: YearMonth,
    monthlyIncome: Double,
    expenses: List<Expense>,
    note: MonthlyCloseNote,
    onSaveNote: (String) -> Unit,
    onExport: () -> Unit
) {
    var notes by rememberSaveable(note.period, note.notes) { mutableStateOf(note.notes) }
    val spendingByCurrency = expenses.filterNot { it.isTransfer }
        .groupBy { Currency.fromCode(it.currencyCode) }
        .mapValues { (_, entries) -> entries.sumOf { it.amount } }
    val primarySpending = spendingByCurrency[Currency.getDefault()] ?: 0.0
    val savings = monthlyIncome - primarySpending
    val topTransactions = expenses.filterNot { it.isTransfer }.sortedByDescending { it.amount }.take(3)
    val topMerchants = expenses.filterNot { it.isTransfer }
        .filter { it.description.isNotBlank() }
        .groupBy { it.description.trim().lowercase(Locale.getDefault()) }
        .mapValues { (_, entries) -> entries.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Monthly Close Review",
                subtitle = "Finalize ${currentPeriod.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))}"
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                MetricCard("Income", formatCurrencyRounded(monthlyIncome), Modifier.weight(1f))
                MetricCard("Saved", formatCurrencyRounded(savings), Modifier.weight(1f))
                MetricCard("Rate", if (monthlyIncome > 0) String.format(Locale.US, "%.1f%%", savings / monthlyIncome * 100.0) else "0%", Modifier.weight(1f))
            }
            spendingByCurrency.forEach { (currency, total) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Spent in ${currency.code}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(formatCurrencyRounded(total, currency), style = MaterialTheme.typography.labelLarge)
                }
            }
            if (topMerchants.isNotEmpty()) {
                SectionHeader(title = "Top Merchants", subtitle = "Highest merchants this month")
                topMerchants.forEach { (merchant, total) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(merchant.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium)
                        Text(formatCurrencyRounded(total), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (topTransactions.isNotEmpty()) {
                SectionHeader(title = "Biggest Transactions", subtitle = "Largest individual expenses")
                topTransactions.forEach { expense ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(expense.category, style = MaterialTheme.typography.bodyMedium)
                        Text(formatCurrencyRounded(expense.amount, Currency.fromCode(expense.currencyCode)), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (monthlyIncome <= 0.0) {
                Text(
                    text = "Monthly income is missing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Month notes") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.medium
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Button(onClick = { onSaveNote(notes) }, modifier = Modifier.weight(1f), shape = Shapes.medium) {
                    Text("Save Notes")
                }
                OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f), shape = Shapes.medium) {
                    Text("Export CSV/PDF")
                }
            }
        }
    }
}

@Composable
private fun ComparisonMetric(
    label: String,
    current: Double,
    previous: Double,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = Shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
        Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(label.uppercase(Locale.US), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatCurrencyRounded(current), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = deltaLabel(current - previous),
                style = MaterialTheme.typography.bodySmall,
                color = if (current - previous >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun IncomeHistoryCard(
    incomeEntries: List<IncomeEntry>,
    onEditEntry: (IncomeEntry) -> Unit,
    onAddEntry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Income History",
                subtitle = "Edit month-by-month income values"
            )
            Button(onClick = onAddEntry, modifier = Modifier.fillMaxWidth(), shape = Shapes.medium) {
                Text("Edit Month")
            }
            if (incomeEntries.isEmpty()) {
                Text("No income history yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                incomeEntries.forEach { entry ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditEntry(entry) },
                        shape = Shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(entry.period, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "Manual value",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(formatCurrency(entry.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthEndForecastCard(
    currentPeriod: YearMonth,
    monthlyIncome: Double,
    totalSpending: Double,
    remainingDays: Int
) {
    val dailyAverage = if (remainingDays > 0) totalSpending / (currentPeriod.atEndOfMonth().dayOfMonth - remainingDays) else totalSpending
    val projectedSpending = totalSpending + (dailyAverage * remainingDays)
    val projectedRemaining = monthlyIncome - projectedSpending
    val isOverBudget = projectedRemaining < 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Month-End Forecast",
                subtitle = "Projected balance based on current spending pace"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Projected Balance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatCurrency(projectedRemaining),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isOverBudget) "You may exceed your budget" else "You should stay within budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconCircle(
                    icon = Icons.Default.CalendarMonth,
                    tint = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    size = 52.dp,
                    iconSize = 24.dp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                MetricCard("Daily Avg", formatCurrencyRounded(dailyAverage), Modifier.weight(1f))
                MetricCard("Remaining Days", remainingDays.toString(), Modifier.weight(1f))
                MetricCard("Projected", formatCurrencyRounded(projectedSpending), Modifier.weight(1f))
            }

            if (remainingDays > 0) {
                Text(
                    text = "Based on ${remainingDays} days remaining with current daily average",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TopMerchantsCard(
    expenses: List<Expense>
) {
    val merchantTotals = expenses
        .filter { it.description.isNotBlank() }
        .groupBy { it.description.trim().lowercase() }
        .mapValues { (_, entries) -> entries.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SectionHeader(
                title = "Top Merchants",
                subtitle = "Where you spend the most"
            )

            if (merchantTotals.isEmpty()) {
                Text("No merchant data available yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                merchantTotals.forEachIndexed { index, (merchant, total) ->
                    val percentage = if (expenses.sumOf { it.amount } > 0) (total / expenses.sumOf { it.amount }) * 100 else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconCircle(
                                icon = Icons.Default.Store,
                                tint = CategoryColors[index % CategoryColors.size],
                                size = 36.dp,
                                iconSize = 18.dp
                            )
                            Column {
                                Text(
                                    text = merchant.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f%% of spending", percentage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = formatCurrencyRounded(total),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (index < merchantTotals.size - 1) {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                    }
                }
            }
        }
    }
}

private data class MonthComparison(
    val currentIncome: Double,
    val previousIncome: Double,
    val currentSpent: Double,
    val previousSpent: Double
) {
    val currentRemaining: Double get() = currentIncome - currentSpent
    val previousRemaining: Double get() = previousIncome - previousSpent
}

private fun buildSpendingInsights(
    expenses: List<Expense>,
    incomeEntries: List<IncomeEntry>,
    categoryBudgets: List<com.financetracker.data.model.CategoryBudget>,
    currentPeriod: YearMonth,
    currentMonthlyIncome: Double
): List<String> {
    val previousPeriod = currentPeriod.minusMonths(1)
    val currentExpenses = expenses.filter { YearMonth.from(it.date) == currentPeriod && !it.isTransfer }
    val previousExpenses = expenses.filter { YearMonth.from(it.date) == previousPeriod && !it.isTransfer }
    val insights = mutableListOf<String>()

    val currentByCategory = currentExpenses.groupBy { it.category }.mapValues { (_, entries) -> entries.sumOf { it.amount } }
    val previousByCategory = previousExpenses.groupBy { it.category }.mapValues { (_, entries) -> entries.sumOf { it.amount } }
    currentByCategory.toList()
        .sortedByDescending { it.second }
        .take(4)
        .forEach { (category, currentAmount) ->
            val previousAmount = previousByCategory[category] ?: 0.0
            if (previousAmount > 0.0 && currentAmount > previousAmount * 1.2) {
                insights += "$category spending increased by ${String.format(Locale.US, "%.0f%%", ((currentAmount - previousAmount) / previousAmount) * 100.0)} compared with last month."
            }
        }

    currentExpenses.groupBy { it.paymentMethod }
        .mapValues { (_, entries) -> entries.sumOf { it.amount } }
        .maxByOrNull { it.value }
        ?.takeIf { it.value > currentExpenses.sumOf { expense -> expense.amount } * 0.45 }
        ?.let { insights += "${it.key} is carrying most of this month's spending." }

    val currentIncome = incomeEntries.firstOrNull { it.period == currentPeriod.toString() }?.amount ?: currentMonthlyIncome
    val previousIncome = incomeEntries.firstOrNull { it.period == previousPeriod.toString() }?.amount ?: 0.0
    val currentSavings = currentIncome - currentExpenses.sumOf { it.amount }
    val previousSavings = previousIncome - previousExpenses.sumOf { it.amount }
    if (currentIncome > 0.0 && currentSavings > previousSavings) {
        insights += "Savings improved compared with the previous month."
    }

    categoryBudgets.filter { it.period == currentPeriod.toString() }.forEach { budget ->
        val spent = currentByCategory[budget.category] ?: 0.0
        if (budget.amount > 0.0 && spent >= budget.amount * 0.85 && spent <= budget.amount) {
            insights += "${budget.category} is close to exceeding its budget."
        } else if (budget.amount > 0.0 && spent > budget.amount) {
            insights += "${budget.category} has exceeded its budget."
        }
    }

    val day = if (currentPeriod == YearMonth.now()) LocalDate.now().dayOfMonth else currentPeriod.lengthOfMonth()
    val projectedSpend = if (day > 0) currentExpenses.sumOf { it.amount } / day * currentPeriod.lengthOfMonth() else 0.0
    if (currentIncome > 0.0 && projectedSpend > currentIncome) {
        insights += "Spending is projected to exceed income before month end."
    }

    return insights.distinct().take(6)
}

private fun buildEffectiveBudgetsForReports(
    budgets: List<com.financetracker.data.model.CategoryBudget>,
    expenses: List<Expense>,
    rolloverSettings: List<CategoryRolloverSetting>,
    selectedMonth: YearMonth
): Map<String, Double> {
    val settingsByCategory = rolloverSettings.associateBy { it.category.lowercase(Locale.getDefault()) }
    return budgets
        .filter { it.period == selectedMonth.toString() }
        .associate { budget ->
            val setting = settingsByCategory[budget.category.lowercase(Locale.getDefault())]
            val effectiveAmount = if (setting?.enabled == true) {
                val previousMonth = selectedMonth.minusMonths(1)
                val previousBudget = budgets.firstOrNull {
                    it.period == previousMonth.toString() && it.category.equals(budget.category, ignoreCase = true)
                }?.amount ?: 0.0
                val previousSpent = expenses
                    .filter { YearMonth.from(it.date) == previousMonth && it.category.equals(budget.category, ignoreCase = true) }
                    .sumOf { it.amount }
                val rollover = previousBudget - previousSpent
                budget.amount + if (rollover >= 0.0 || setting.carryOverspend) rollover else 0.0
            } else {
                budget.amount
            }
            budget.category to effectiveAmount
        }
}

private fun buildMonthComparison(
    expenses: List<Expense>,
    incomeEntries: List<IncomeEntry>,
    currentPeriod: YearMonth
): MonthComparison {
    val previousPeriod = currentPeriod.minusMonths(1)
    val currentSpent = expenses.filter { YearMonth.from(it.date) == currentPeriod }.sumOf { it.amount }
    val previousSpent = expenses.filter { YearMonth.from(it.date) == previousPeriod }.sumOf { it.amount }
    val currentIncome = incomeEntries.firstOrNull { it.period == currentPeriod.toString() }?.amount ?: 0.0
    val previousIncome = incomeEntries.firstOrNull { it.period == previousPeriod.toString() }?.amount ?: 0.0
    return MonthComparison(currentIncome, previousIncome, currentSpent, previousSpent)
}

private fun deltaLabel(delta: Double): String {
    val prefix = if (delta >= 0) "+" else "-"
    return "$prefix${formatCurrencyRounded(kotlin.math.abs(delta))} vs last month"
}

private fun Double.toEditableAmount(): String {
    if (this <= 0.0) return ""
    val integerValue = toLong()
    return if (this == integerValue.toDouble()) integerValue.toString() else toString()
}
