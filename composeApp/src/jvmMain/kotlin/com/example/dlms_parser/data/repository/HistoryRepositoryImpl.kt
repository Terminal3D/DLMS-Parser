package com.example.dlms_parser.data.repository

import com.example.dlms_parser.domain.model.ParseHistory
import com.example.dlms_parser.domain.model.ParseHistoryEntry
import com.example.dlms_parser.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class HistoryRepositoryImpl : HistoryRepository {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
        classDiscriminator = "messageClass"
    }
    
    private val historyFile = File(System.getProperty("user.home"), ".dlms_parser_history.json")
    
    private val _history = MutableStateFlow(loadHistory())
    
    override fun getHistory(): Flow<ParseHistory> = _history.asStateFlow()
    
    override suspend fun saveHistoryEntry(entry: ParseHistoryEntry) {
        val currentHistory = _history.value
        val updatedHistory = currentHistory.addEntry(entry)
        _history.value = updatedHistory
        saveHistoryToFile(updatedHistory)
    }
    
    override suspend fun removeHistoryEntry(id: String) {
        val currentHistory = _history.value
        val updatedHistory = currentHistory.removeEntry(id)
        _history.value = updatedHistory
        saveHistoryToFile(updatedHistory)
    }
    
    override suspend fun clearHistory() {
        val clearedHistory = _history.value.clearHistory()
        _history.value = clearedHistory
        saveHistoryToFile(clearedHistory)
    }
    
    private fun loadHistory(): ParseHistory {
        return try {
            if (historyFile.exists()) {
                val jsonString = historyFile.readText()
                json.decodeFromString<ParseHistory>(jsonString)
            } else {
                ParseHistory()
            }
        } catch (e: Exception) {
            println("Failed to load history: ${e.message}")
            ParseHistory()
        }
    }
    
    private fun saveHistoryToFile(history: ParseHistory) {
        try {
            val jsonString = json.encodeToString(history)
            historyFile.writeText(jsonString)
        } catch (e: IOException) {
            println("Failed to save history: ${e.message}")
        }
    }
}