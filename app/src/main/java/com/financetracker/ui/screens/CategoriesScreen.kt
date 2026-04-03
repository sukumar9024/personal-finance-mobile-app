package com.financetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financetracker.data.model.Category
import com.financetracker.data.model.Currency
import com.financetracker.ui.theme.CardElevation
import com.financetracker.ui.theme.ScreenPadding
import com.financetracker.ui.theme.Shapes
import com.financetracker.ui.theme.Spacing
import com.financetracker.ui.theme.categoryColor
import com.financetracker.ui.theme.formatCurrency
import com.financetracker.ui.theme.formatCurrencyRounded
import com.financetracker.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    val categoryState = uiState.categoryState
    val spendByCategory = uiState.expenses.groupBy { it.category }.mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    val totalSpent = uiState.totalAmount
    val totalBudget = uiState.monthlyIncome
    val totalRemaining = uiState.monthlyIncome - uiState.totalAmount
    val currency = uiState.currency
    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColor by remember { mutableStateOf("") }
    var selectedCategoryForBudget by remember { mutableStateOf<Category?>(null) }
    var budgetInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Categories", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = ScreenPadding, vertical = Spacing.md)
        ) {
            when {
                categoryState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                }
                categoryState.categories.isEmpty() -> {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text("No categories available", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text("Categories will appear here once configured.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        CategoriesSummaryCard(
                            totalBudget = totalBudget,
                            totalSpent = totalSpent,
                            totalRemaining = totalRemaining,
                            currency = currency
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(categoryState.categories) { category ->
                                CategoryItemCard(
                                    category = category,
                                    spentAmount = spendByCategory[category.name] ?: 0.0,
                                    monthlyIncome = totalBudget,
                                    currency = currency,
                                    onClick = {
                                        selectedCategoryForBudget = category
                                        budgetInput = category.monthlyBudget?.toEditableAmount().orEmpty()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category name") },
                        singleLine = true,
                        shape = Shapes.medium
                    )
                    OutlinedTextField(
                        value = newCategoryColor,
                        onValueChange = { newCategoryColor = it },
                        label = { Text("Color hex (optional)") },
                        placeholder = { Text("#10B981") },
                        singleLine = true,
                        shape = Shapes.medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCategory(newCategoryName, newCategoryColor)
                        newCategoryName = ""
                        newCategoryColor = ""
                        showAddDialog = false
                    },
                    enabled = newCategoryName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newCategoryName = ""
                    newCategoryColor = ""
                    showAddDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    val editingCategory = selectedCategoryForBudget
    if (editingCategory != null) {
        AlertDialog(
            onDismissRequest = { selectedCategoryForBudget = null },
            title = { Text("Category Budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        text = editingCategory.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { value ->
                            budgetInput = value.filter { it.isDigit() || it == '.' }
                        },
                        label = { Text("Monthly budget") },
                        placeholder = { Text("Set category budget") },
                        singleLine = true,
                        shape = Shapes.medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCategoryBudget(
                            editingCategory.name,
                            budgetInput.toDoubleOrNull()
                        )
                        selectedCategoryForBudget = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedCategoryForBudget = null
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun CategoriesSummaryCard(
    totalBudget: Double,
    totalSpent: Double,
    totalRemaining: Double,
    currency: Currency
) {
    val progress = if (totalBudget > 0.0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Monthly Budget Snapshot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                if (totalBudget > 0.0) "${formatCurrency(totalSpent, currency)} spent from ${formatCurrency(totalBudget, currency)} this month" else "Set your monthly income in Settings to track your budget.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(Shapes.full)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(12.dp)
                        .clip(Shapes.full)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            )
                        )
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CategorySummaryMetric("Budget", formatCurrencyRounded(totalBudget, currency), Modifier.weight(1f))
                CategorySummaryMetric("Spent", formatCurrencyRounded(totalSpent, currency), Modifier.weight(1f))
                CategorySummaryMetric("Remaining", formatCurrencyRounded(totalRemaining, currency), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CategorySummaryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = Shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)) {
        Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryItemCard(
    category: Category,
    spentAmount: Double,
    monthlyIncome: Double,
    currency: Currency,
    onClick: () -> Unit
) {
    val accent = categoryColor(category)
    val shareText = if (monthlyIncome > 0.0 && spentAmount > 0.0) {
        "${((spentAmount / monthlyIncome) * 100).toInt()}% of overall income"
    } else {
        "No spending recorded this month"
    }
    val categoryBudget = category.monthlyBudget ?: 0.0
    val utilization = if (categoryBudget > 0.0) (spentAmount / categoryBudget).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(colors = listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.08f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Category, contentDescription = category.name, tint = accent, modifier = Modifier.size(26.dp))
                }
                Surface(shape = Shapes.medium, color = accent.copy(alpha = 0.12f)) {
                    Text(
                        text = if (spentAmount > 0.0) formatCurrencyRounded(spentAmount, currency) else "${currency.symbol}0",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(shareText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(shape = Shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Spent", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatCurrency(spentAmount, currency), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Budget", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (categoryBudget > 0.0) formatCurrency(categoryBudget, currency) else "Tap to set",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (categoryBudget > 0.0) MaterialTheme.colorScheme.onSurface else accent
                        )
                    }
                    if (categoryBudget > 0.0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(Shapes.full)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(utilization)
                                    .height(6.dp)
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

private fun Double.toEditableAmount(): String {
    val integerValue = toLong()
    return if (this == integerValue.toDouble()) integerValue.toString() else toString()
}
