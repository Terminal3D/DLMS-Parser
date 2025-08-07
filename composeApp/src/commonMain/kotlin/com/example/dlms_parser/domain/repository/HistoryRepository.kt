package com.example.dlms_parser.domain.repository

import com.example.dlms_parser.domain.model.ParseHistory
import com.example.dlms_parser.domain.model.ParseHistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(): Flow<ParseHistory>
    suspend fun saveHistoryEntry(entry: ParseHistoryEntry)
    suspend fun removeHistoryEntry(id: String)
    suspend fun clearHistory()
}