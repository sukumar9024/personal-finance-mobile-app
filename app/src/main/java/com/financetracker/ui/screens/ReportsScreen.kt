package com.financetracker.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financetracker.ui.theme.*
import com.financetracker.ui.viewmodel.ExpenseViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoryState by viewModel.categoryState.collectAsState()

    // Calculate totals by category
    val expenses = uiState.expenses
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { (_, exp) -> exp.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val totalExpenses = expenses.sumOf { it.amount }
    val formattedTotal = "₹${"%,.2f".format(totalExpenses)}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reports",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Expenses (${uiState.currentMonthSheet.replace("expenses_", "")})",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formattedTotal,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${expenses.size} transactions",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data to display. Add some expenses!",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                return@Column
            }

            // Pie Chart Section
            Text(
                text = "By Category",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (categoryTotals.isNotEmpty()) {
                            PieChart(
                                data = categoryTotals,
                                total = totalExpenses,
                                categories = categoryState.categories
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend
                    categoryTotals.forEach { (category, amount) ->
                        val percentage = (amount / totalExpenses * 100)
                        val categoryColor = categoryState.categories
                            .find { it.name == category }
                            ?.let { Color(android.graphics.Color.parseColor(it.color)) }
                            ?: Purple40

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(categoryColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${"%.1f".format(percentage)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Breakdown List
            Text(
                text = "Category Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    categoryTotals.forEach { (category, amount) ->
                        val categoryColor = categoryState.categories
                            .find { it.name == category }
                            ?.let { Color(android.graphics.Color.parseColor(it.color)) }
                            ?: Purple40

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(categoryColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = category,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "₹${"%,.2f".format(amount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Progress bar for budget (if available)
                        val categoryBudget = categoryState.categories
                            .find { it.name == category }
                            ?.monthlyBudget

                        if (categoryBudget != null && categoryBudget > 0) {
                            val progress = (amount / categoryBudget).toFloat().coerceIn(0f, 1f)
                            val progressColor = if (progress > 1f) MaterialTheme.colorScheme.error
                                            else if (progress > 0.8f) MaterialTheme.colorScheme.tertiary
                                            else MaterialTheme.colorScheme.primary

                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = progressColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Budget: ₹${"%,.0f".format(categoryBudget)}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    data: List<Pair<String, Double>>,
    total: Double,
    categories: List<com.financetracker.data.model.Category>
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        if (total == 0.0) return@Canvas

        val size = size.minDimension
        val radius = size / 2f
        
        var startAngle = -90f
        data.forEach { (category, amount) ->
            val sweepAngle = (amount / total * 360).toFloat()
            val categoryColor = categories
                .find { it.name == category }
                ?.let { Color(android.graphics.Color.parseColor(it.color)) }
                ?: Purple40

            drawPieSlice(
                color = categoryColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                radius = radius
            )
            startAngle += sweepAngle
        }
    }
}

private fun DrawScope.drawPieSlice(
    color: Color,
    startAngle: Float,
    sweepAngle: Float,
    radius: Float
) {
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        size = Size(radius * 2, radius * 2)
    )
}

// Note: categoryState needs to be passed or accessed differently in PieChart
// For simplicity, we'll make it a parameter or use a different approach
// Adjusting the PieChart call to pass categoryState
// (Implementation would need refactoring for cleaner architecture)