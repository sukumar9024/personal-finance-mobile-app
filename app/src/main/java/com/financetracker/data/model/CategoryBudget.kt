package com.financetracker.data.model

/**
 * Represents a budget for a specific category in a specific month.
 * Period format: "YYYY-MM" (e.g., "2024-01" for January 2024)
 */
data class CategoryBudget(
    val id: String = "",
    val category: String,
    val period: String,
    val amount: Double,
    val sheetRowIndex: Int = -1
) {
    fun toJson(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "category" to category,
            "period" to period,
            "amount" to amount
        )
    }

    companion object {
        fun fromJson(json: Map<String, Any?>): CategoryBudget {
            return CategoryBudget(
                id = json["id"] as? String ?: "",
                category = json["category"] as? String ?: "",
                period = json["period"] as? String ?: "",
                amount = (json["amount"] as? Number)?.toDouble() ?: 0.0,
                sheetRowIndex = (json["sheetRowIndex"] as? Number)?.toInt() ?: -1
            )
        }
    }
}