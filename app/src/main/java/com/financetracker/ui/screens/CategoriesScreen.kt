package com.financetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financetracker.data.model.Category
import com.financetracker.data.model.CategoryBudget
import com.financetracker.data.model.Currency
import com.financetracker.ui.theme.CardElevation
import com.financetracker.ui.theme.ScreenPadding
import com.financetracker.ui.theme.Shapes
import com.financetracker.ui.theme.Spacing
import com.financetracker.ui.theme.categoryColor
import com.financetracker.ui.theme.formatCurrency
import com.financetracker.ui.theme.formatCurrencyRounded
import com.financetracker.ui.viewmodel.ExpenseViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    
    // Month selection state
    val currentPeriod = parseCurrentPeriodFromSheet(uiState.currentMonthSheet)
    var selectedMonth by remember { mutableStateOf(currentPeriod) }
    
    // Dialog state
    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColor by remember { mutableStateOf(
        CategoryColorsList.first()
    ) }
    var selectedCategoryForBudget by remember { mutableStateOf<Category?>(null) }
    var budgetInput by remember { mutableStateOf("") }
    var selectedCategoryForColor by remember { mutableStateOf<Category?>(null) }
    
    // Sync month selection with current period when screen is first shown
    LaunchedEffect(currentPeriod) {
        selectedMonth = currentPeriod
    }
    
    // Get category budgets for selected month
    val categoryBudgets = uiState.categoryBudgets.filter { it.period == selectedMonth.toString() }
        .associateBy { it.category }
    
    // Get spend by category for selected month
    val expensesForMonth = uiState.expenses.filter { 
        YearMonth.from(it.date) == selectedMonth 
    }
    val spendByCategory = expensesForMonth.groupBy { it.category }
        .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    
    val currency = uiState.currency

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
                uiState.categoryState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                }
                uiState.categoryState.categories.isEmpty() -> {
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
                        // Month selector Card
                        MonthSelectorCard(
                            selectedMonth = selectedMonth,
                            totalBudget = categoryBudgets.values.sumOf { it.amount },
                            totalSpent = spendByCategory.values.sum(),
                            currency = currency,
                            onMonthChange = { selectedMonth = it }
                        )
                        
                        // Category Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.categoryState.categories) { category ->
                                CategoryItemCard(
                                    category = category,
                                    budget = categoryBudgets[category.name],
                                    spentAmount = spendByCategory[category.name] ?: 0.0,
                                    currency = currency,
                                    onClick = {
                                        selectedCategoryForBudget = category
                                        budgetInput = categoryBudgets[category.name]?.amount?.toEditableAmount().orEmpty()
                                    },
                                    onColorClick = {
                                        selectedCategoryForColor = category
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Category Dialog
    if (showAddDialog) {
        AddCategoryDialog(
            categoryName = newCategoryName,
            onCategoryNameChange = { newCategoryName = it },
            categoryColor = newCategoryColor,
            onCategoryColorChange = { newCategoryColor = it },
            onSave = {
                viewModel.addCategory(newCategoryName, newCategoryColor)
                newCategoryName = ""
                newCategoryColor = ""
                showAddDialog = false
            },
            onDismiss = {
                newCategoryName = ""
                newCategoryColor = ""
                showAddDialog = false
            }
        )
    }

    // Edit Budget Dialog
    val editingCategory = selectedCategoryForBudget
    if (editingCategory != null) {
        EditBudgetDialog(
            categoryName = editingCategory.name,
            selectedMonth = selectedMonth,
            currentBudget = categoryBudgets[editingCategory.name]?.amount,
            budgetInput = budgetInput,
            onBudgetInputChange = { budgetInput = sanitizeDecimalInput(it) },
            onSave = { amount ->
                viewModel.updateCategoryBudgetForMonth(editingCategory.name, selectedMonth.toString(), amount)
                selectedCategoryForBudget = null
            },
            onDismiss = { selectedCategoryForBudget = null }
        )
    }

    // Edit Category Color Dialog
    val colorCategory = selectedCategoryForColor
    if (colorCategory != null) {
        EditCategoryColorDialog(
            category = colorCategory,
            currentColor = colorCategory.color,
            selectedColor = newCategoryColor,
            onColorSelected = { newCategoryColor = it },
            onSave = { color ->
                viewModel.updateCategoryColor(colorCategory.name, color)
                selectedCategoryForColor = null
            },
            onDismiss = { selectedCategoryForColor = null }
        )
    }
}

@Composable
private fun MonthSelectorCard(
    selectedMonth: YearMonth,
    totalBudget: Double,
    totalSpent: Double,
    currency: Currency,
    onMonthChange: (YearMonth) -> Unit
) {
    val progress = if (totalBudget > 0.0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            // Month selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(selectedMonth.minusMonths(1)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = { onMonthChange(selectedMonth.plusMonths(1)) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }
            
            // Budget info
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                MonthSummaryMetric("Budget", formatCurrencyRounded(totalBudget, currency), Modifier.weight(1f))
                MonthSummaryMetric("Spent", formatCurrencyRounded(totalSpent, currency), Modifier.weight(1f))
                MonthSummaryMetric("Remaining", formatCurrencyRounded(totalBudget - totalSpent, currency), Modifier.weight(1f))
            }
            
            // Progress bar
            if (totalBudget > 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(Shapes.full)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(10.dp)
                            .clip(Shapes.full)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthSummaryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = Shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)) {
        Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryItemCard(
    category: Category,
    budget: CategoryBudget?,
    spentAmount: Double,
    currency: Currency,
    onClick: () -> Unit,
    onColorClick: () -> Unit
) {
    val accent = categoryColor(category)
    val budgetAmount = budget?.amount ?: 0.0
    val utilization = if (budgetAmount > 0.0) (spentAmount / budgetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget = spentAmount > budgetAmount && budgetAmount > 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            // Icon and spent amount row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(colors = listOf(accent.copy(alpha = 0.2f), accent.copy(alpha = 0.05f))))
                        .clickable(onClick = onColorClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Category, contentDescription = category.name, tint = accent, modifier = Modifier.size(24.dp))
                }
                Surface(shape = Shapes.medium, color = if (isOverBudget) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else accent.copy(alpha = 0.12f)) {
                    Text(
                        text = if (spentAmount > 0.0) formatCurrencyRounded(spentAmount, currency) else "${currency.symbol}0",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else accent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Category name
            Text(category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)

            // Budget section
            if (budgetAmount > 0.0) {
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
                            Text("Budget", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                formatCurrency(budgetAmount, currency),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Progress bar
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
                                    .background(if (isOverBudget) MaterialTheme.colorScheme.error else accent)
                            )
                        }
                    }
                }
            } else {
                Text("No budget set", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            // Color change hint
            Text(
                text = "Tap icon to change color",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Color palette options for categories
 */
val CategoryColorsList = listOf(
    "#FF5722", // Deep Orange
    "#2196F3", // Blue
    "#E91E63", // Pink
    "#9C27B0", // Purple
    "#FF9800", // Orange
    "#4CAF50", // Green
    "#3F51B5", // Indigo
    "#10B981", // Emerald
    "#F97316", // Amber
    "#607D8B", // Blue Grey
    "#F44336", // Red
    "#00BCD4", // Cyan
    "#8BC34A", // Light Green
    "#FFC107", // Amber/Yellow
    "#795548", // Brown
    "#673AB7"  // Deep Purple
)

@Composable
private fun ColorPickerRow(
    colors: List<String>,
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(colors) { colorHex ->
            val color = try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }
            val isSelected = colorHex == selectedColor
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onColorSelected(colorHex) }
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                        } else {
                            Modifier
                        }
                    ),
                color = color,
                contentColor = Color.White
            ) {
                if (isSelected) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    categoryName: String,
    onCategoryNameChange: (String) -> Unit,
    categoryColor: String,
    onCategoryColorChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = onCategoryNameChange,
                    label = { Text("Category name") },
                    singleLine = true,
                    shape = Shapes.medium
                )
                Text("Choose color:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ColorPickerRow(
                    colors = CategoryColorsList,
                    selectedColor = categoryColor,
                    onColorSelected = onCategoryColorChange
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = categoryName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditBudgetDialog(
    categoryName: String,
    selectedMonth: YearMonth,
    currentBudget: Double?,
    budgetInput: String,
    onBudgetInputChange: (String) -> Unit,
    onSave: (Double?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Category Budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Surface(shape = Shapes.medium, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = onBudgetInputChange,
                    label = { Text("Budget for ${selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}") },
                    placeholder = { Text("Enter amount") },
                    singleLine = true,
                    shape = Shapes.medium
                )
                Text(
                    text = currentBudget?.let { "Current budget: ${formatCurrency(it)}" } ?: "No budget set for this month",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Leave blank to remove budget for this month.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(budgetInput.toDoubleOrNull()) },
                enabled = budgetInput.isBlank() || budgetInput.toDoubleOrNull() != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditCategoryColorDialog(
    category: Category,
    currentColor: String,
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val accent = try {
        Color(android.graphics.Color.parseColor(currentColor))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Category Color") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                // Current color preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap a color below to preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text("Choose color:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ColorPickerRow(
                    colors = CategoryColorsList,
                    selectedColor = selectedColor,
                    onColorSelected = onColorSelected
                )
                
                // Selected color preview
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val previewColor = try {
                        Color(android.graphics.Color.parseColor(selectedColor))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(previewColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = selectedColor,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedColor) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun sanitizeDecimalInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')
    return if (firstDot == -1) {
        filtered
    } else {
        filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", "")
    }
}

private fun Double.toEditableAmount(): String {
    val integerValue = toLong()
    return if (this == integerValue.toDouble()) integerValue.toString() else toString()
}

private fun parseCurrentPeriodFromSheet(sheetName: String): YearMonth {
    val rawPeriod = sheetName.removePrefix("expenses_").replace("_", "-")
    return runCatching { YearMonth.parse(rawPeriod) }.getOrElse { YearMonth.now() }
}