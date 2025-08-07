package com.example.dlms_parser.domain.usecase

import com.example.dlms_parser.domain.repository.DlmsParserRepository

class ValidateHexDataUseCase(
    private val repository: DlmsParserRepository
) {
    fun execute(hexData: String): Boolean {
        return repository.validateHexFormat(hexData)
    }
}