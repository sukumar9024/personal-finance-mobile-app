package com.financetracker.data.model

data class SavingsGoal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currencyCode: String = Currency.getDefault().code
)
