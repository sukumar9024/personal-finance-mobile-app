package com.financetracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financetracker.data.model.Category
import com.financetracker.data.model.Expense
import com.financetracker.data.repository.GoogleSheetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalAmount: Double = 0.0,
    val currentMonthSheet: String = ""
)

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GoogleSheetsRepository(application)

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _categoryState = MutableStateFlow(CategoryUiState())
    val categoryState: StateFlow<CategoryUiState> = _categoryState.asStateFlow()

    init {
        loadCurrentMonthSheet()
        loadCategories()
        loadExpenses()
    }

    private fun loadCurrentMonthSheet() {
        viewModelScope.launch {
            val sheetName = repository.getCurrentMonthSheetName()
            _uiState.value = _uiState.value.copy(currentMonthSheet = sheetName)
        }
    }

    fun loadExpenses() {
        val sheetName = _uiState.value.currentMonthSheet
        if (sheetName.isBlank()) {
            viewModelScope.launch {
                val newSheetName = repository.getCurrentMonthSheetName()
                _uiState.value = _uiState.value.copy(currentMonthSheet = newSheetName)
            }
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            // Ensure sheet exists first
            val sheetCreated = repository.ensureMonthSheetExists(sheetName)
            if (sheetCreated) {
                val expenses = repository.fetchExpenses(sheetName)
                val total = expenses.sumOf { it.amount }
                _uiState.value = _uiState.value.copy(
                    expenses = expenses.sortedByDescending { it.date },
                    totalAmount = total,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load expenses. Please check your connection and spreadsheet access."
                )
            }
        }
    }

    fun loadCategories() {
        _categoryState.value = _categoryState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val categories = repository.fetchCategories()
            _categoryState.value = _categoryState.value.copy(
                categories = categories,
                isLoading = false
            )
        }
    }

    fun addExpense(expense: Expense, onComplete: (Boolean) -> Unit) {
        val sheetName = _uiState.value.currentMonthSheet
        if (sheetName.isBlank()) {
            onComplete(false)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val success = repository.addExpense(sheetName, expense)
            if (success) {
                loadExpenses() // Refresh list
                onComplete(true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to add expense. Please try again."
                )
                onComplete(false)
            }
        }
    }

    fun updateExpense(expense: Expense, onComplete: (Boolean) -> Unit) {
        val sheetName = _uiState.value.currentMonthSheet
        if (sheetName.isBlank()) {
            onComplete(false)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val updatedExpense = expense.copy(modifiedAt = java.time.LocalDateTime.now())
            val success = repository.updateExpense(sheetName, updatedExpense)
            if (success) {
                loadExpenses()
                onComplete(true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update expense. Please try again."
                )
                onComplete(false)
            }
        }
    }

    fun deleteExpense(expense: Expense, onComplete: (Boolean) -> Unit) {
        val sheetName = _uiState.value.currentMonthSheet
        if (sheetName.isBlank() || expense.sheetRowIndex <= 0) {
            onComplete(false)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val success = repository.deleteExpense(sheetName, expense.sheetRowIndex)
            if (success) {
                loadExpenses()
                onComplete(true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete expense. Please try again."
                )
                onComplete(false)
            }
        }
    }

    fun getMonthlySummary(): Map<String, Double> {
        val sheetName = _uiState.value.currentMonthSheet
        if (sheetName.isBlank()) return emptyMap()

        // Blocking call for simplicity - in production, use a proper StateFlow
        return runBlocking {
            repository.fetchMonthlySummary(sheetName)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}