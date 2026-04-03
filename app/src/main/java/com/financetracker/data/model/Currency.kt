package com.financetracker.data.model

import java.util.Locale

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    INR("INR", "₹", "Indian Rupee"),
    JPY("JPY", "¥", "Japanese Yuan"),
    CNY("CNY", "¥", "Chinese Yuan"),
    AUD("AUD", "A$", "Australian Dollar"),
    CAD("CAD", "C$", "Canadian Dollar"),
    SGD("SGD", "S$", "Singapore Dollar"),
    AED("AED", "د.إ", "UAE Dirham");

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