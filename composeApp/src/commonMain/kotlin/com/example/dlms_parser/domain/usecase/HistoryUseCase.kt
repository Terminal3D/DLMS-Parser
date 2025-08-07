package com.example.dlms_parser.domain.usecase

import com.example.dlms_parser.domain.model.DlmsMessage
import com.example.dlms_parser.domain.model.ParseHistory
import com.example.dlms_parser.domain.model.ParseHistoryEntry
import com.example.dlms_parser.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import java.util.*

class HistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    fun getHistory(): Flow<ParseHistory> {
        return historyRepository.getHistory()
    }
    
    suspend fun saveParseResult(
        inputData: String,
        messages: List<DlmsMessage>,
        success: Boolean,
        errorMessage: String? = null
    ) {
        val entry = ParseHistoryEntry(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            inputData = inputData,
            messages = messages,
            success = success,
            errorMessage = errorMessage
        )
        historyRepository.saveHistoryEntry(entry)
    }
    
    suspend fun removeHistoryEntry(id: String) {
        historyRepository.removeHistoryEntry(id)
    }
    
    suspend fun clearHistory() {
        historyRepository.clearHistory()
    }
}