package com.financetracker.data.model

enum class RecurringType {
    EXPENSE,
    INCOME
}

data class RecurringEntry(
    val id: String,
    val title: String,
    val amount: Double,
    val type: RecurringType,
    val dayOfMonth: Int,
    val category: String? = null,
    val description: String = "",
    val paymentMethod: String = "Cash",
    val active: Boolean = true,
    val sheetRowIndex: Int = -1
)
