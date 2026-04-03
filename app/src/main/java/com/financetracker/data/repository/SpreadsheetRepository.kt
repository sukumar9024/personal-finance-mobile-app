package com.financetracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.financetracker.data.model.SpreadsheetConfig
import org.json.JSONArray
import org.json.JSONObject

class SpreadsheetRepository(context: Context) {

    private val prefs: SharedPreferences = 
        context.getSharedPreferences("spreadsheet_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SPREADSHEETS = "spreadsheets"
        private const val KEY_ACTIVE_SPREADSHEET_ID = "active_spreadsheet_id"
    }

    fun getAllSpreadsheets(): List<SpreadsheetConfig> {
        val json = prefs.getString(KEY_SPREADSHEETS, null) ?: return emptyList()
        val jsonArray = JSONArray(json)
        val spreadsheets = mutableListOf<SpreadsheetConfig>()
        
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            spreadsheets.add(
                SpreadsheetConfig(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    spreadsheetId = obj.getString("spreadsheetId"),
                    isActive = obj.getBoolean("isActive"),
                    createdAt = obj.getLong("createdAt"),
                    lastSyncedAt = if (obj.has("lastSyncedAt")) obj.getLong("lastSyncedAt") else null
                )
            )
        }
        return spreadsheets
    }

    fun getActiveSpreadsheet(): SpreadsheetConfig? {
        return getAllSpreadsheets().find { it.isActive }
    }

    fun addSpreadsheet(spreadsheet: SpreadsheetConfig) {
        val spreadsheets = getAllSpreadsheets().toMutableList()
        
        // If this is set as active, deactivate others
        if (spreadsheet.isActive) {
            spreadsheets.forEachIndexed { index, config ->
                spreadsheets[index] = config.copy(isActive = false)
            }
        }
        
        spreadsheets.add(spreadsheet)
        saveSpreadsheets(spreadsheets)
    }

    fun setActiveSpreadsheet(spreadsheetId: String) {
        val spreadsheets = getAllSpreadsheets().toMutableList()
        spreadsheets.forEachIndexed { index, config ->
            spreadsheets[index] = config.copy(isActive = config.id == spreadsheetId)
        }
        saveSpreadsheets(spreadsheets)
    }

    fun deleteSpreadsheet(spreadsheetId: String) {
        val spreadsheets = getAllSpreadsheets().toMutableList()
        spreadsheets.removeAll { it.id == spreadsheetId }
        saveSpreadsheets(spreadsheets)
    }

    fun updateLastSynced(spreadsheetId: String, timestamp: Long) {
        val spreadsheets = getAllSpreadsheets().toMutableList()
        spreadsheets.forEachIndexed { index, config ->
            if (config.id == spreadsheetId) {
                spreadsheets[index] = config.copy(lastSyncedAt = timestamp)
            }
        }
        saveSpreadsheets(spreadsheets)
    }

    private fun saveSpreadsheets(spreadsheets: List<SpreadsheetConfig>) {
        val jsonArray = JSONArray()
        spreadsheets.forEach { spreadsheet ->
            val obj = JSONObject().apply {
                put("id", spreadsheet.id)
                put("name", spreadsheet.name)
                put("spreadsheetId", spreadsheet.spreadsheetId)
                put("isActive", spreadsheet.isActive)
                put("createdAt", spreadsheet.createdAt)
                spreadsheet.lastSyncedAt?.let { put("lastSyncedAt", it) }
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_SPREADSHEETS, jsonArray.toString()).apply()
    }
}