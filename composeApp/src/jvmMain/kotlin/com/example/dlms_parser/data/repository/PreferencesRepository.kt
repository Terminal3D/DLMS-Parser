package com.example.dlms_parser.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataStore
import com.example.dlms_parser.domain.model.ParseHistory
import com.example.dlms_parser.domain.model.ParseHistoryEntry
import com.example.dlms_parser.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) : HistoryRepository {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
        classDiscriminator = "messageClass"
    }
    
    override fun getHistory(): Flow<ParseHistory> = dataStore.data.map { preferences ->
        val historyJson = preferences[HISTORY_KEY] ?: ""
        if (historyJson.isNotBlank()) {
            try {
                json.decodeFromString<ParseHistory>(historyJson)
            } catch (e: Exception) {
                println("Failed to decode history: ${e.message}")
                ParseHistory()
            }
        } else {
            ParseHistory()
        }
    }
    
    override suspend fun saveHistoryEntry(entry: ParseHistoryEntry) {
        dataStore.edit { preferences ->
            val currentHistoryJson = preferences[HISTORY_KEY] ?: ""
            val currentHistory = if (currentHistoryJson.isNotBlank()) {
                try {
                    json.decodeFromString<ParseHistory>(currentHistoryJson)
                } catch (e: Exception) {
                    ParseHistory()
                }
            } else {
                ParseHistory()
            }
            
            val updatedHistory = currentHistory.addEntry(entry)
            val updatedHistoryJson = json.encodeToString(updatedHistory)
            preferences[HISTORY_KEY] = updatedHistoryJson
        }
    }
    
    override suspend fun removeHistoryEntry(id: String) {
        dataStore.edit { preferences ->
            val currentHistoryJson = preferences[HISTORY_KEY] ?: ""
            val currentHistory = if (currentHistoryJson.isNotBlank()) {
                try {
                    json.decodeFromString<ParseHistory>(currentHistoryJson)
                } catch (e: Exception) {
                    ParseHistory()
                }
            } else {
                ParseHistory()
            }
            
            val updatedHistory = currentHistory.removeEntry(id)
            val updatedHistoryJson = json.encodeToString(updatedHistory)
            preferences[HISTORY_KEY] = updatedHistoryJson
        }
    }
    
    override suspend fun clearHistory() {
        dataStore.edit { preferences ->
            preferences[HISTORY_KEY] = json.encodeToString(ParseHistory())
        }
    }
    
    fun isDarkTheme(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY] ?: false
    }
    
    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDark
        }
    }
    
    companion object {
        private val HISTORY_KEY = stringPreferencesKey("dlms_history")
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }
}