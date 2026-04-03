package com.financetracker.data.model

data class SpreadsheetConfig(
    val id: String,
    val name: String,
    val spreadsheetId: String,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)