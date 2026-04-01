package com.financetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financetracker.ui.viewmodel.ExpenseViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var spreadsheetId by remember { mutableStateOf(BuildConfig.SPREADSHEET_ID) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Google Sheets Configuration",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        OutlinedTextField(
                            value = spreadsheetId,
                            onValueChange = { spreadsheetId = it },
                            label = { Text("Spreadsheet ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter spreadsheet ID from URL") }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "The Spreadsheet ID is the part between /d/ and /edit in your Google Sheets URL",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Current Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Month Sheet:")
                            Text(
                                uiState.currentMonthSheet,
                                fontWeight = FontWeight.Medium,
                                color = Purple40
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Expenses:")
                            Text(
                                "₹${"%,.2f".format(uiState.totalAmount)}",
                                fontWeight = FontWeight.Bold,
                                color = Purple40
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Categories Loaded:")
                            Text(
                                "${uiState.categoryState.categories.size}",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "About",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Text(
                            text = "Finance Tracker v1.0",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "A personal finance app that syncs with Google Sheets",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        OutlinedButton(
                            onClick = { showInfoDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("How to Set Up")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        // Manually refresh data
                        viewModel.loadExpenses()
                        viewModel.loadCategories()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Refresh Data", fontSize = 16.sp)
                }
            }

            item {
                Text(
                    text = "Note: This app uses a Service Account to access Google Sheets. Make sure your spreadsheet is shared with the service account email with Editor permissions.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Setup Instructions") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("1. Create a new Google Sheet", fontWeight = FontWeight.Bold)
                    Text("   - Open Google Sheets and create a new spreadsheet")
                    Text("")
                    Text("2. Create required sheets", fontWeight = FontWeight.Bold)
                    Text("   - Add a sheet named 'categories' with columns: Name, Color, Monthly Budget")
                    Text("   - Monthly expense sheets will be created automatically (expenses_YYYY_MM)")
                    Text("")
                    Text("3. Share with Service Account", fontWeight = FontWeight.Bold)
                    Text("   - Share the spreadsheet with your service account email (from JSON key)")
                    Text("   - Give Editor permissions")
                    Text("")
                    Text("4. Get Spreadsheet ID", fontWeight = FontWeight.Bold)
                    Text("   - From URL: https://docs.google.com/spreadsheets/d/<ID>/edit")
                    Text("   - Copy the ID part and paste in Settings")
                    Text("")
                    Text("5. Add Service Account JSON key to app", fontWeight = FontWeight.Bold)
                    Text("   - Place service-account-key.json in app/src/main/assets/")
                    Text("   - Or set SPREADSHEET_ID and SERVICE_ACCOUNT_JSON in build.gradle.kts")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}