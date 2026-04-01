package com.financetracker.data.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Expense(
    val id: String = "",
    val date: LocalDate,
    val amount: Double,
    val category: String,
    val subcategory: String? = null,
    val description: String = "",
    val paymentMethod: String = "Cash",
    val receiptUrl: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now(),
    val sheetRowIndex: Int = -1 // Track row in Google Sheets for updates
)