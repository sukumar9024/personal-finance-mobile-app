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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.financetracker.data.model.Currency
import com.financetracker.data.model.Expense
import com.financetracker.data.model.TransactionType
import com.financetracker.ui.theme.CardShape
import com.financetracker.ui.theme.FinanceSectionHeader
import com.financetracker.ui.theme.Shapes
import com.financetracker.ui.theme.Spacing
import com.financetracker.ui.theme.categoryColor
import com.financetracker.ui.theme.formatCurrency
import com.financetracker.ui.viewmodel.ExpenseViewModel
import com.financetracker.ui.viewmodel.SplitExpenseInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

private enum class EntryMode(val label: String) {
    EXPENSE("Expense"),
    TRANSFER("Transfer"),
    SPLIT("Split")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currency = uiState.currency

    var entryMode by remember { mutableStateOf(EntryMode.EXPENSE) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var subcategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var transferAccount by remember { mutableStateOf("") }
    var transferDestination by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    // Split mode state
    var splitRows by remember { mutableStateOf(listOf(SplitExpenseInput("Other", 0.0))) }

    val categories = uiState.categoryState.categories
    val accountOptions = listOf("Cash", "Bank", "UPI", "Credit Card", "Debit Card", "Wallet", "Other")

    if (selectedCategory.isBlank() && categories.isNotEmpty()) {
        selectedCategory = categories.first().name
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = LocalDate.of(year, month + 1, dayOfMonth)
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    )

    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val isValid = when (entryMode) {
        EntryMode.EXPENSE -> amount.isNotBlank() && amountValue > 0 && selectedCategory.isNotBlank()
        EntryMode.TRANSFER -> amount.isNotBlank() && amountValue > 0 && transferAccount.isNotBlank() && transferDestination.isNotBlank()
        EntryMode.SPLIT -> splitRows.any { it.amount > 0 } && splitRows.all { it.category.isNotBlank() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Add Transaction",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = entryMode.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                modifier = Modifier.shadow(elevation = 4.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = Shapes.large
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (isValid && !saving) {
                                saving = true
                                when (entryMode) {
                                    EntryMode.EXPENSE -> {
                                        val expense = Expense(
                                            id = UUID.randomUUID().toString(),
                                            date = date,
                                            amount = amountValue,
                                            category = selectedCategory,
                                            subcategory = subcategory.takeIf { it.isNotBlank() },
                                            description = description,
                                            paymentMethod = paymentMethod,
                                            transactionType = TransactionType.EXPENSE,
                                            tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                        )
                                        viewModel.addExpense(expense)
                                    }
                                    EntryMode.TRANSFER -> {
                                        val expense = Expense(
                                            id = UUID.randomUUID().toString(),
                                            date = date,
                                            amount = amountValue,
                                            category = "Transfer",
                                            description = description.ifBlank { "Transfer from $transferAccount to $transferDestination" },
                                            paymentMethod = transferAccount,
                                            transferAccount = transferAccount,
                                            transferDestinationAccount = transferDestination,
                                            transactionType = TransactionType.TRANSFER,
                                            tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                        )
                                        viewModel.addExpense(expense)
                                    }
                                    EntryMode.SPLIT -> {
                                        val expenses = viewModel.buildSplitExpenses(
                                            date = date,
                                            paymentMethod = paymentMethod,
                                            description = description,
                                            tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                            splitRows = splitRows.filter { it.amount > 0 }
                                        )
                                        viewModel.addExpenseGroup(expenses)
                                    }
                                }
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
                                text = "Save",
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
            // Entry Mode Selector
            InputSection(
                title = "Transaction Type",
                subtitle = "Choose how you want to record this transaction"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    EntryMode.entries.forEach { mode ->
                        val isSelected = entryMode == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { entryMode = mode },
                            shape = Shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.md),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Date and Amount
            AmountPreviewCard(
                amount = amountValue,
                date = date,
                currency = currency,
                onDateClick = { datePickerDialog.show() }
            )

            InputSection(
                title = "Amount",
                subtitle = "Enter the transaction amount"
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Amount") },
                    leadingIcon = {
                        Text(
                            text = currency.symbol,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                )
            }

            // Mode-specific fields
            when (entryMode) {
                EntryMode.EXPENSE -> {
                    InputSection(
                        title = "Category",
                        subtitle = "Select expense category"
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = Shapes.medium
                            )
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { category ->
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
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = Shapes.medium
                        )
                    }
                }

                EntryMode.TRANSFER -> {
                    InputSection(
                        title = "Transfer Details",
                        subtitle = "Specify source and destination accounts"
                    ) {
                        OutlinedTextField(
                            value = transferAccount,
                            onValueChange = { transferAccount = it },
                            label = { Text("From Account") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = Shapes.medium
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        OutlinedTextField(
                            value = transferDestination,
                            onValueChange = { transferDestination = it },
                            label = { Text("To Account") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = Shapes.medium
                        )
                    }
                }

                EntryMode.SPLIT -> {
                    InputSection(
                        title = "Split Details",
                        subtitle = "Divide amount across categories"
                    ) {
                        splitRows.forEachIndexed { index, row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var splitCategoryExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = splitCategoryExpanded,
                                    onExpandedChange = { splitCategoryExpanded = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = row.category,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Category") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = splitCategoryExpanded)
                                        },
                                        modifier = Modifier.menuAnchor(),
                                        shape = Shapes.medium
                                    )
                                    DropdownMenu(
                                        expanded = splitCategoryExpanded,
                                        onDismissRequest = { splitCategoryExpanded = false }
                                    ) {
                                        categories.forEach { category ->
                                            DropdownMenuItem(
                                                text = { Text(category.name) },
                                                onClick = {
                                                    splitRows = splitRows.toMutableList().apply {
                                                        set(index, row.copy(category = category.name))
                                                    }
                                                    splitCategoryExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = if (row.amount > 0) row.amount.toString() else "",
                                    onValueChange = { value ->
                                        val newAmount = value.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                                        splitRows = splitRows.toMutableList().apply {
                                            set(index, row.copy(amount = newAmount))
                                        }
                                    },
                                    label = { Text("Amount") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = Shapes.medium
                                )

                                if (splitRows.size > 1) {
                                    IconButton(
                                        onClick = {
                                            splitRows = splitRows.toMutableList().apply { removeAt(index) }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            if (index < splitRows.size - 1) {
                                Spacer(modifier = Modifier.height(Spacing.sm))
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        OutlinedButton(
                            onClick = {
                                splitRows = splitRows + SplitExpenseInput("Other", 0.0)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = Shapes.medium
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text("Add Split Row")
                        }
                    }
                }
            }

            // Payment Method (for expense and split)
            if (entryMode != EntryMode.TRANSFER) {
                InputSection(
                    title = "Account",
                    subtitle = "Select payment method"
                ) {
                    ExposedDropdownMenuBox(
                        expanded = paymentExpanded,
                        onExpandedChange = { paymentExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = Shapes.medium
                        )
                        DropdownMenu(
                            expanded = paymentExpanded,
                            onDismissRequest = { paymentExpanded = false }
                        ) {
                            accountOptions.forEach { method ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (paymentMethod == method) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.size(16.dp))
                                            }
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
                }
            }

            // Description and Tags
            InputSection(
                title = "Additional Details",
                subtitle = "Add description and tags"
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.medium
                )
            }
        }
    }
}

@Composable
internal fun AmountPreviewCard(
    amount: Double,
    date: LocalDate,
    currency: Currency,
    onDateClick: () -> Unit
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatCurrency(amount, currency),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Surface(
                modifier = Modifier.clickable(onClick = onDateClick),
                shape = Shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
internal fun InputSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            FinanceSectionHeader(title = title, subtitle = subtitle, showDivider = true)
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}
