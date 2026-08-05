package com.financetracker.data.model

data class TransactionTemplate(
    val id: String,
    val name: String,
    val amount: Double,
    val currencyCode: String = Currency.getDefault().code,
    val category: String,
    val subcategory: String? = null,
    val description: String = "",
    val paymentMethod: String = "Cash"
)
