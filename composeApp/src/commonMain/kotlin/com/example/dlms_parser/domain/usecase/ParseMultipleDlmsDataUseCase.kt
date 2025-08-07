package com.example.dlms_parser.domain.usecase

import com.example.dlms_parser.domain.model.DlmsMessage
import com.example.dlms_parser.domain.model.ParseResult
import com.example.dlms_parser.domain.repository.DlmsParserRepository

class ParseMultipleDlmsDataUseCase(
    private val repository: DlmsParserRepository
) {
    suspend fun execute(hexDataList: List<String>): ParseResult<List<DlmsMessage>> {
        val invalidHexData = hexDataList.filter { !repository.validateHexFormat(it) }
        if (invalidHexData.isNotEmpty()) {
            return ParseResult.Error("Invalid hex format in data: ${invalidHexData.joinToString(", ")}")
        }
        
        return repository.parseMultipleHexData(hexDataList)
    }
}