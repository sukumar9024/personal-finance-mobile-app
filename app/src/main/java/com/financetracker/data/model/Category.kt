package com.financetracker.data.model

data class Category(
    val name: String,
    val color: String = "#FF6200EE", // Default purple
    val icon: String = "default",
    val monthlyBudget: Double? = null
)