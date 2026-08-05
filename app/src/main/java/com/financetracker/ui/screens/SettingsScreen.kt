package com.financetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financetracker.ui.theme.ScreenPadding
import com.financetracker.ui.theme.Shapes
import com.financetracker.ui.theme.Spacing
import com.financetracker.ui.theme.ThemeMode
import com.financetracker.ui.viewmodel.ExpenseViewModel
import com.financetracker.data.model.Currency
import com.financetracker.data.model.CsvImportMapping

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val showInfoDialog = remember { mutableStateOf(false) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    var csvText by remember { mutableStateOf("") }
    var csvMapping by remember(uiState.csvImportMapping) { mutableStateOf(uiState.csvImportMapping) }
    val csvPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        csvText = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
        }.getOrDefault("")
        showCsvImportDialog = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = ScreenPadding, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
item {
    SettingsSection(
        title = "Appearance",
        subtitle = "Choose how the app should look"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            ThemeOption(
                title = "System",
                icon = Icons.Default.BrightnessAuto,
                selected = uiState.themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                modifier = Modifier.weight(1f)
            )
            ThemeOption(
                title = "Light",
                icon = Icons.Default.LightMode,
                selected = uiState.themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                modifier = Modifier.weight(1f)
            )
            ThemeOption(
                title = "Dark",
                icon = Icons.Default.DarkMode,
                selected = uiState.themeMode == ThemeMode.DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

item {
    SettingsSection(
        title = "Currency",
        subtitle = "Select your preferred currency"
    ) {
                        val expanded = remember { mutableStateOf(false) }
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = Shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded.value = !expanded.value }
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Currency",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${uiState.currency.displayName} (${uiState.currency.symbol})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                androidx.compose.material3.DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Currency.entries.forEach { currency ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currency.symbol,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = currency.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (uiState.currency == currency) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.setCurrency(currency)
                                expanded.value = false
                            }
                        )
                    }
                }
            }
        }
    }
}

item {
    SettingsSection(
        title = "Reports",
        subtitle = "Choose how dashboard and reports calculate totals"
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Include transfers",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Transfers are excluded from savings by default.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                androidx.compose.material3.Switch(
                    checked = uiState.includeTransfersInReports,
                    onCheckedChange = viewModel::setIncludeTransfersInReports
                )
            }
        }
    }
}

item {
    SettingsSection(
        title = "Security & Conversion",
        subtitle = "Protect the app and choose optional currency conversion"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SettingsSwitchRow(
                title = "Biometric lock",
                subtitle = "Works on debug installs when biometrics or device PIN is enrolled.",
                checked = uiState.biometricLockEnabled,
                onCheckedChange = viewModel::setBiometricLockEnabled
            )
            SettingsSwitchRow(
                title = "Exchange conversion",
                subtitle = "Show converted spending totals using your manual rates.",
                checked = uiState.exchangeConversionEnabled,
                onCheckedChange = viewModel::setExchangeConversionEnabled
            )
        }
    }
}

item {
    SettingsSection(
        title = "Dashboard Layout",
        subtitle = "Show or hide dashboard cards you use most"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            uiState.dashboardCardPreferences.sortedBy { it.sortOrder }.forEachIndexed { index, preference ->
                DashboardCardPreferenceRow(
                    title = preference.title,
                    group = preference.group,
                    visible = preference.visible,
                    canMoveUp = index > 0,
                    canMoveDown = index < uiState.dashboardCardPreferences.lastIndex,
                    onVisibleChange = { viewModel.setDashboardCardVisibility(preference.id, it) },
                    onMoveUp = { viewModel.moveDashboardCard(preference.id, -1) },
                    onMoveDown = { viewModel.moveDashboardCard(preference.id, 1) }
                )
            }
        }
    }
}

item {
    SettingsSection(
        title = "Data Tools",
        subtitle = "Import, validate, and diagnose finance data"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            OutlinedButton(
                onClick = { csvPickerLauncher.launch("text/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = Shapes.medium
            ) {
                Text("Import CSV Statement")
            }
            ValidationSummaryCard(
                title = "Data Validation",
                rows = viewModel.buildDataValidationIssues().map { "${it.severity}: ${it.title} - ${it.detail}" }
                    .ifEmpty { listOf("No data quality issues found.") }
            )
            ValidationSummaryCard(
                title = "Google Sheets Setup Validator",
                rows = viewModel.buildSetupChecks().map {
                    "${if (it.passed) "Passed" else "Needs attention"}: ${it.title} - ${it.detail}"
                }
            )
        }
    }
}

            item {
                SettingsSection(
                    title = "About",
                    subtitle = "App information and Google Sheets setup"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = Shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.md),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Version",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "1.0.0",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showInfoDialog.value = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = Shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text("Setup Guide")
                        }

                        Text(
                            text = "Budget, sync status, and manual refresh now live on the home screen for quicker access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Google Sheets sync requires proper configuration. Make sure to share your spreadsheet with the service account email as Editor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showCsvImportDialog) {
        CsvImportDialog(
            csvText = csvText,
            mapping = csvMapping,
            onMappingChange = { csvMapping = it },
            onImport = {
                viewModel.importCsvText(
                    csvText = csvText,
                    mapping = csvMapping,
                    defaultCategory = uiState.categoryState.categories.firstOrNull()?.name ?: "Other",
                    defaultAccount = "Bank",
                    defaultCurrency = uiState.currency
                )
                showCsvImportDialog = false
            },
            onDismiss = { showCsvImportDialog = false }
        )
    }

    if (showInfoDialog.value) {
        AlertDialog(
            onDismissRequest = { showInfoDialog.value = false },
            title = { Text("Google Sheets Setup Guide") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    SetupStep(number = "1", text = "Create a Google Sheet for tracking your expenses.")
                    SetupStep(number = "2", text = "Add a 'categories' tab with columns: Name, Color, and Monthly Budget.")
                    SetupStep(number = "3", text = "Share the sheet with your service account email as Editor.")
                    SetupStep(number = "4", text = "Copy the spreadsheet ID from the URL and add it to local.properties as spreadsheet.id.")
                    SetupStep(number = "5", text = "Place your service account JSON in app/src/main/assets/service-account-key.json.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog.value = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got it!")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column {
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
        Spacer(modifier = Modifier.height(Spacing.sm))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeOption(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = Shapes.large,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            androidx.compose.material3.Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun DashboardCardPreferenceRow(
    title: String,
    group: String,
    visible: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var dragAmount by remember { mutableStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(canMoveUp, canMoveDown) {
                detectVerticalDragGestures(
                    onDragEnd = { dragAmount = 0f },
                    onVerticalDrag = { _, dragDelta ->
                        dragAmount += dragDelta
                        when {
                            dragAmount < -36f && canMoveUp -> {
                                onMoveUp()
                                dragAmount = 0f
                            }
                            dragAmount > 36f && canMoveDown -> {
                                onMoveDown()
                                dragAmount = 0f
                            }
                        }
                    }
                )
            },
        shape = Shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = "$group • ${if (visible) "visible" else "hidden"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move $title up")
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move $title down")
            }
            androidx.compose.material3.Switch(
                checked = visible,
                onCheckedChange = onVisibleChange
            )
        }
    }
}

@Composable
private fun ValidationSummaryCard(
    title: String,
    rows: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            rows.take(8).forEach { row ->
                Text(row, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CsvImportDialog(
    csvText: String,
    mapping: CsvImportMapping,
    onMappingChange: (CsvImportMapping) -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import CSV Statement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = if (csvText.isBlank()) "No CSV text loaded." else "CSV loaded. Confirm the column names used by your statement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MappingField("Date column", mapping.dateColumn) { onMappingChange(mapping.copy(dateColumn = it)) }
                MappingField("Amount column", mapping.amountColumn) { onMappingChange(mapping.copy(amountColumn = it)) }
                MappingField("Description column", mapping.descriptionColumn) { onMappingChange(mapping.copy(descriptionColumn = it)) }
                MappingField("Category column", mapping.categoryColumn) { onMappingChange(mapping.copy(categoryColumn = it)) }
                MappingField("Account column", mapping.accountColumn) { onMappingChange(mapping.copy(accountColumn = it)) }
                MappingField("Currency column", mapping.currencyColumn) { onMappingChange(mapping.copy(currencyColumn = it)) }
            }
        },
        confirmButton = {
            Button(onClick = onImport, enabled = csvText.isNotBlank()) {
                Text("Import")
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
private fun MappingField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.medium
    )
}

@Composable
private fun SetupStep(
    number: String,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = Shapes.small,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
