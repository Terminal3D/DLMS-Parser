package com.example.dlms_parser.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ParseHistoryEntry(
    val id: String,
    val timestamp: Long,
    val inputData: String,
    val messages: List<DlmsMessage>,
    val success: Boolean,
    val errorMessage: String? = null
)

@Serializable
data class ParseHistory(
    val entries: List<ParseHistoryEntry> = emptyList()
) {
    fun addEntry(entry: ParseHistoryEntry): ParseHistory {
        val updatedEntries = (listOf(entry) + entries).take(100)
        return copy(entries = updatedEntries)
    }
    
    fun removeEntry(id: String): ParseHistory {
        return copy(entries = entries.filter { it.id != id })
    }
    
    fun clearHistory(): ParseHistory {
        return copy(entries = emptyList())
    }
}