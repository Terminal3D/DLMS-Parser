package com.example.dlms_parser.domain.usecase

import com.example.dlms_parser.domain.model.DlmsMessage
import com.example.dlms_parser.domain.model.ParseResult
import com.example.dlms_parser.domain.repository.DlmsParserRepository

class ParseDlmsDataUseCase(
    private val repository: DlmsParserRepository
) {
    suspend fun execute(hexData: String): ParseResult<DlmsMessage> {
        if (!repository.validateHexFormat(hexData)) {
            return ParseResult.Error("Invalid hex format")
        }
        
        return repository.parseHexData(hexData)
    }
}