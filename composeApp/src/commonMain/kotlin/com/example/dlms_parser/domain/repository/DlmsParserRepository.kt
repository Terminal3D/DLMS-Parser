package com.example.dlms_parser.domain.repository

import com.example.dlms_parser.domain.model.DlmsMessage
import com.example.dlms_parser.domain.model.ParseResult

interface DlmsParserRepository {
    suspend fun parseHexData(hexData: String): ParseResult<DlmsMessage>
    suspend fun parseMultipleHexData(hexDataList: List<String>): ParseResult<List<DlmsMessage>>
    fun validateHexFormat(hexData: String): Boolean
}