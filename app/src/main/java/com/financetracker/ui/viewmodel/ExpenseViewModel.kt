package com.financetracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financetracker.data.model.Category
import com.financetracker.data.model.Expense
import com.financetracker.data.repository.GoogleSheetsRepository
import com.financetracker.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

data class FinanceTrackerUiState(
    val expenses: List<Expense> = emptyList(),
    val categoryState: CategoryState = CategoryState(),
    val currentMonthSheet: String = "",
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GoogleSheetsRepository(application)

    private val _uiState = MutableStateFlow(
        FinanceTrackerUiState(
            currentMonthSheet = repository.getCurrentMonthSheetName()
        )
    )
    val uiState: StateFlow<FinanceTrackerUiState> = _uiState.asStateFlow()

    init {
        refreshAllData()
    }

    fun loadExpenses() {
        val currentSheet = _uiState.value.currentMonthSheet.ifBlank { repository.getCurrentMonthSheetName() }

        if (!repository.isReadyForLiveSync()) {
            _uiState.value = _uiState.value.copy(
                currentMonthSheet = currentSheet,
                isLoading = false,
                totalAmount = _uiState.value.expenses.sumOf { it.amount },
                errorMessage = repository.getConfigurationStatusMessage()
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            runCatching {
                repository.fetchExpenses(currentSheet)
            }.onSuccess { expenses ->
                _uiState.value = _uiState.value.copy(
                    currentMonthSheet = currentSheet,
                    expenses = expenses,
                    totalAmount = expenses.sumOf { it.amount },
                    isLoading = false,
                    errorMessage = repository.getConfigurationStatusMessage()
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    currentMonthSheet = currentSheet,
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load expenses from Google Sheets."
                )
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                categoryState = _uiState.value.categoryState.copy(isLoading = true)
            )

            runCatching {
                repository.fetchCategories()
            }.onSuccess { categories ->
                _uiState.value = _uiState.value.copy(
                    categoryState = CategoryState(
                        categories = categories,
                        isLoading = false
                    ),
                    errorMessage = repository.getConfigurationStatusMessage()
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    categoryState = _uiState.value.categoryState.copy(isLoading = false),
                    errorMessage = error.message ?: "Failed to load categories."
                )
            }
        }
    }

    fun addExpense(expense: Expense) {
        if (!repository.isReadyForLiveSync()) {
            val newExpenses = _uiState.value.expenses + expense
            _uiState.value = _uiState.value.copy(
                expenses = newExpenses,
                totalAmount = newExpenses.sumOf { it.amount },
                errorMessage = repository.getConfigurationStatusMessage()
            )
            return
        }

        viewModelScope.launch {
            val currentSheet = _uiState.value.currentMonthSheet.ifBlank { repository.getCurrentMonthSheetName() }
            runCatching {
                repository.addExpense(currentSheet, expense)
            }.onSuccess {
                loadExpenses()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to add expense to Google Sheets."
                )
            }
        }
    }

    fun updateExpense(expense: Expense) {
        if (!repository.isReadyForLiveSync()) {
            val newExpenses = _uiState.value.expenses.map {
                if (it.id == expense.id) expense else it
            }
            _uiState.value = _uiState.value.copy(
                expenses = newExpenses,
                totalAmount = newExpenses.sumOf { it.amount },
                errorMessage = repository.getConfigurationStatusMessage()
            )
            return
        }

        viewModelScope.launch {
            val currentSheet = _uiState.value.currentMonthSheet.ifBlank { repository.getCurrentMonthSheetName() }
            runCatching {
                repository.updateExpense(currentSheet, expense)
            }.onSuccess {
                loadExpenses()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to update expense in Google Sheets."
                )
            }
        }
    }

    fun deleteExpense(id: String) {
        if (!repository.isReadyForLiveSync()) {
            val newExpenses = _uiState.value.expenses.filter { it.id != id }
            _uiState.value = _uiState.value.copy(
                expenses = newExpenses,
                totalAmount = newExpenses.sumOf { it.amount },
                errorMessage = repository.getConfigurationStatusMessage()
            )
            return
        }

        viewModelScope.launch {
            val currentSheet = _uiState.value.currentMonthSheet.ifBlank { repository.getCurrentMonthSheetName() }
            runCatching {
                repository.deleteExpense(currentSheet, id)
            }.onSuccess {
                loadExpenses()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Failed to delete expense from Google Sheets."
                )
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = themeMode)
    }

    private fun refreshAllData() {
        viewModelScope.launch {
            val currentSheet = repository.getCurrentMonthSheetName()
            val categories = runCatching { repository.fetchCategories() }
                .getOrDefault(defaultCategories())

            val expenses = if (repository.isReadyForLiveSync()) {
                runCatching { repository.fetchExpenses(currentSheet) }.getOrDefault(emptyList())
            } else {
                emptyList()
            }

            _uiState.value = _uiState.value.copy(
                currentMonthSheet = currentSheet,
                categoryState = CategoryState(
                    categories = categories,
                    isLoading = false
                ),
                expenses = expenses,
                totalAmount = expenses.sumOf { it.amount },
                isLoading = false,
                errorMessage = repository.getConfigurationStatusMessage()
            )
        }
    }

    private fun defaultCategories(): List<Category> {
        return listOf(
            Category("Food", "#FF5722", "restaurant", 10000.0),
            Category("Transport", "#2196F3", "directions_car", 5000.0),
            Category("Shopping", "#E91E63", "shopping_bag", 8000.0),
            Category("Bills", "#9C27B0", "receipt", 15000.0),
            Category("Entertainment", "#FF9800", "movie", 5000.0),
            Category("Health", "#4CAF50", "local_hospital", 3000.0),
            Category("Education", "#3F51B5", "school", 2000.0),
            Category("Other", "#607D8B", "more_horiz", 0.0)
        )
    }
}
