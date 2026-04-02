package com.financetracker.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.financetracker.data.model.Expense
import com.financetracker.ui.theme.Shapes
import com.financetracker.ui.theme.Spacing
import com.financetracker.ui.theme.categoryColor
import com.financetracker.ui.theme.formatCurrency
import com.financetracker.ui.viewmodel.ExpenseViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var subcategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var tags by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val paymentMethods = listOf("Cash", "Card", "UPI", "Bank Transfer", "Wallet", "Other")

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = LocalDate.of(year, month + 1, dayOfMonth)
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    )

    val isValid = amount.isNotBlank() && selectedCategory.isNotBlank()
    val amountValue = amount.toDoubleOrNull() ?: 0.0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Expense",
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
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .shadow(elevation = 4.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (isValid && !saving) {
                                saving = true
                                val expense = Expense(
                                    id = UUID.randomUUID().toString(),
                                    date = date,
                                    amount = amountValue,
                                    category = selectedCategory,
                                    subcategory = subcategory.takeIf { it.isNotBlank() },
                                    description = description,
                                    paymentMethod = paymentMethod,
                                    tags = tags.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() }
                                )
                                viewModel.addExpense(expense)
                                saving = false
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = isValid && !saving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        shape = Shapes.large
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = "Save Expense",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Amount Preview Card
            AmountPreviewCard(
                amount = amountValue,
                date = date,
                onDateClick = { datePickerDialog.show() }
            )

            // Amount Input Section
            InputSection(
                title = "Amount & Category",
                subtitle = "Enter the expense details"
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Amount") },
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(),
                    shape = Shapes.medium
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        placeholder = { Text("Select a category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = Shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        uiState.categoryState.categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = categoryColor(category),
                                                    shape = Shapes.full
                                                )
                                        )
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category.name
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                OutlinedTextField(
                    value = subcategory,
                    onValueChange = { subcategory = it },
                    label = { Text("Subcategory (Optional)") },
                    placeholder = { Text("e.g., Dining, Fuel, etc.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                )
            }

            // Additional Details Section
            InputSection(
                title = "Additional Details",
                subtitle = "Add more context to your expense"
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("What was this expense for?") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                ExposedDropdownMenuBox(
                    expanded = paymentExpanded,
                    onExpandedChange = { paymentExpanded = it }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = Shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = paymentExpanded,
                        onDismissRequest = { paymentExpanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check.takeIf { paymentMethod == method },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(method)
                                    }
                                },
                                onClick = {
                                    paymentMethod = method
                                    paymentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (Optional)") },
                    placeholder = { Text("e.g., work, travel (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                )
            }
        }
    }
}

import androidx.compose.ui.unit.sp

@Composable
fun AmountPreviewCard(
    amount: Double,
    date: LocalDate,
    onDateClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(amount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Surface(
                modifier = Modifier.clickable(onClick = onDateClick),
                shape = Shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("dd MMM")),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun InputSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            content()
        }
    }
}