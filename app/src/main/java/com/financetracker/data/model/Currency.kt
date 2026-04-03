package com.financetracker.data.model

import java.util.Locale

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "\u20AC", "Euro"),
    GBP("GBP", "\u00A3", "British Pound"),
    INR("INR", "\u20B9", "Indian Rupee"),
    JPY("JPY", "\u00A5", "Japanese Yen"),
    CNY("CNY", "\u00A5", "Chinese Yuan"),
    AUD("AUD", "A$", "Australian Dollar"),
    CAD("CAD", "C$", "Canadian Dollar"),
    SGD("SGD", "S$", "Singapore Dollar"),
    AED("AED", "AED", "UAE Dirham");

    companion object {
        fun fromCode(code: String): Currency {
            return entries.find { it.code == code } ?: getDefault()
        }

        fun getDefault(): Currency {
            val locale = Locale.getDefault()
            return when (locale.country) {
                "IN" -> INR
                "US" -> USD
                "GB" -> GBP
                "EU", "DE", "FR", "IT", "ES" -> EUR
                "JP" -> JPY
                "CN" -> CNY
                "AU" -> AUD
                "CA" -> CAD
                "SG" -> SGD
                "AE" -> AED
                else -> USD
            }
        }
    }
}
