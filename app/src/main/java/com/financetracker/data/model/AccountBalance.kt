package com.financetracker.data.model

data class AccountBalance(
    val name: String,
    val amount: Double,
    val isDebt: Boolean = false
)
