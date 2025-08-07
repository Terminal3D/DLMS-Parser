package com.example.dlms_parser.domain.usecase

import com.example.dlms_parser.domain.model.DlmsMessage
import com.example.dlms_parser.domain.model.ParseResult
import com.example.dlms_parser.domain.model.ViewFormat
import com.example.dlms_parser.domain.repository.ExportRepository

class ExportMessagesUseCase(
    private val exportRepository: ExportRepository
) {
    suspend fun exportToJson(messages: List<DlmsMessage>): ParseResult<String> {
        return exportRepository.exportToJson(messages)
    }
    
    suspend fun exportToFormat(messages: List<DlmsMessage>, format: ViewFormat): ParseResult<String> {
        return exportRepository.exportToFormat(messages, format)
    }
    
    suspend fun copyToClipboard(messages: List<DlmsMessage>): ParseResult<Unit> {
        return when (val jsonResult = exportRepository.exportToJson(messages)) {
            is ParseResult.Success -> exportRepository.copyToClipboard(jsonResult.data)
            is ParseResult.Error -> ParseResult.Error(jsonResult.message, jsonResult.exception)
        }
    }
    
    suspend fun copyToClipboardInFormat(messages: List<DlmsMessage>, format: ViewFormat): ParseResult<Unit> {
        return when (val exportResult = exportRepository.exportToFormat(messages, format)) {
            is ParseResult.Success -> exportRepository.copyToClipboard(exportResult.data)
            is ParseResult.Error -> ParseResult.Error(exportResult.message, exportResult.exception)
        }
    }
    
    suspend fun copyTextToClipboard(text: String): ParseResult<Unit> {
        return exportRepository.copyToClipboard(text)
    }
    
    suspend fun saveToFile(messages: List<DlmsMessage>, fileName: String = "dlms_messages.json"): ParseResult<String> {
        return when (val jsonResult = exportRepository.exportToJson(messages)) {
            is ParseResult.Success -> exportRepository.saveToFile(jsonResult.data, fileName)
            is ParseResult.Error -> ParseResult.Error(jsonResult.message, jsonResult.exception)
        }
    }
    
    suspend fun saveToFileInFormat(messages: List<DlmsMessage>, format: ViewFormat, fileName: String? = null): ParseResult<String> {
        val defaultFileName = when (format) {
            ViewFormat.JSON -> "dlms_messages.json"
            ViewFormat.XML -> "dlms_messages.xml"
        }
        return when (val exportResult = exportRepository.exportToFormat(messages, format)) {
            is ParseResult.Success -> exportRepository.saveToFile(exportResult.data, fileName ?: defaultFileName)
            is ParseResult.Error -> ParseResult.Error(exportResult.message, exportResult.exception)
        }
    }
}