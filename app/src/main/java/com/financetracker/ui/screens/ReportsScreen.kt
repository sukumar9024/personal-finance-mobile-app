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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.financetracker.ui.theme.CardShape
import com.financetracker.ui.theme.FinanceHeroCard
import com.financetracker.ui.theme.FinanceSectionHeader
import com.financetracker.ui.theme.FinanceStatPill
import com.financetracker.ui.theme.ScreenPadding
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reports", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Visualize category performance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            FinanceHeroCard(modifier = Modifier.fillMaxWidth()) {
                FinanceSectionHeader(
                    title = "Monthly report",
                    subtitle = "Break down how this month's expenses are distributed"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = formatCurrency(totalExpenses),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FinanceStatPill(
                        label = "Transactions",
                        value = "${expenses.size}",
                        modifier = Modifier.weight(1f)
                    )
                    FinanceStatPill(
                        label = "Top Category",
                        value = highestCategory,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (expenses.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "Nothing to analyze yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add a few expenses and your category analytics will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                return@Column
            }

            FinanceSectionHeader(
                title = "Category split",
                subtitle = "A quick visual of where the money is concentrated"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DonutChart(
                            data = categoryTotals,
                            total = totalExpenses,
                            categories = categoryState.categories
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Top spend",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCurrencyRounded(highestSpend),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    categoryTotals.forEach { (category, amount) ->
                        val percentage = if (totalExpenses > 0) amount / totalExpenses * 100 else 0.0
                        val accent = categoryColor(categoryState.categories.find { it.name == category })

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(accent)
                                )
                                Column {
                                    Text(category, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = formatCurrency(amount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = String.format(Locale.US, "%.1f%%", percentage),
                                style = MaterialTheme.typography.titleSmall,
                                color = accent
                            )
                        }
                    }
                }
            }

            FinanceSectionHeader(
                title = "Budget progress",
                subtitle = "Track category budgets against actual spend"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    categoryTotals.forEach { (category, amount) ->
                        val categoryModel = categoryState.categories.find { it.name == category }
                        val budget = categoryModel?.monthlyBudget
                        val accent = categoryColor(categoryModel)

                        Column(modifier = Modifier.padding(vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(category, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = formatCurrency(amount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = if (budget != null && budget > 0.0) {
                                        "${((amount / budget) * 100).toInt()}%"
                                    } else {
                                        "No budget"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = accent
                                )
                            }

                            if (budget != null && budget > 0.0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = (amount / budget).toFloat().coerceIn(0f, 1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(99.dp)),
                                    color = accent,
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
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    data: List<Pair<String, Double>>,
    total: Double,
    categories: List<Category>
) {
    Canvas(modifier = Modifier.size(220.dp)) {
        if (data.isEmpty() || total <= 0.0) return@Canvas

        val diameter = size.minDimension
        val strokeWidth = diameter * 0.18f
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
            startAngle += sweepAngle
        }
    }
}
