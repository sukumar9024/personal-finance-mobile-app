package com.financetracker.data.model

/**
 * Represents a spending category (e.g., Food, Transport, Shopping).
 * Budgets are now managed per-month via the CategoryBudget model.
 */
data class Category(
    val name: String,
    val color: String = "#FF6200EE", // Default purple
    val icon: String = "default"
)
