package com.financetracker.data.model

data class IncomeEntry(
    val period: String,
    val amount: Double,
    val recurringEntryId: String? = null,
    val sheetRowIndex: Int = -1
)
