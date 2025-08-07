package com.example.dlms_parser.domain.repository

import com.example.dlms_parser.domain.model.DlmsMessage
import com.example.dlms_parser.domain.model.ParseResult
import com.example.dlms_parser.domain.model.ViewFormat

interface ExportRepository {
    suspend fun exportToJson(messages: List<DlmsMessage>): ParseResult<String>
    suspend fun exportToXml(messages: List<DlmsMessage>): ParseResult<String>
    suspend fun exportToFormat(messages: List<DlmsMessage>, format: ViewFormat): ParseResult<String>
    suspend fun copyToClipboard(text: String): ParseResult<Unit>
    suspend fun saveToFile(content: String, fileName: String): ParseResult<String>
}