package com.example.dlms_parser.data.repository

import com.example.dlms_parser.data.parser.GuruxDlmsParser
import com.example.dlms_parser.domain.model.DlmsMessage
import com.example.dlms_parser.domain.model.ParseResult
import com.example.dlms_parser.domain.repository.DlmsParserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DlmsParserRepositoryImpl(
    private val parser: GuruxDlmsParser = GuruxDlmsParser()
) : DlmsParserRepository {
    
    override suspend fun parseHexData(hexData: String): ParseResult<DlmsMessage> {
        return withContext(Dispatchers.IO) {
            parser.parseHexData(hexData)
        }
    }
    
    override suspend fun parseMultipleHexData(hexDataList: List<String>): ParseResult<List<DlmsMessage>> {
        return withContext(Dispatchers.IO) {
            try {
                val results = mutableListOf<DlmsMessage>()
                val errors = mutableListOf<String>()
                
                hexDataList.forEachIndexed { index, hexData ->
                    when (val result = parser.parseHexData(hexData)) {
                        is ParseResult.Success -> results.add(result.data)
                        is ParseResult.Error -> errors.add("Index $index: ${result.message}")
                    }
                }
                
                if (errors.isNotEmpty()) {
                    ParseResult.Error("Failed to parse some messages: ${errors.joinToString("; ")}")
                } else {
                    ParseResult.Success(results)
                }
            } catch (e: Exception) {
                ParseResult.Error("Failed to parse multiple hex data", e.message)
            }
        }
    }
    
    override fun validateHexFormat(hexData: String): Boolean {
        return parser.validateHexFormat(hexData)
    }
}