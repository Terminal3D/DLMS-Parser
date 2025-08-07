package com.example.dlms_parser.domain

import com.example.dlms_parser.data.repository.DlmsParserRepositoryImpl
import com.example.dlms_parser.domain.model.ParseResult
import com.example.dlms_parser.domain.usecase.ParseDlmsDataUseCase
import com.example.dlms_parser.domain.usecase.ParseMultipleDlmsDataUseCase
import com.example.dlms_parser.domain.usecase.ValidateHexDataUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DlmsUseCaseTest {
    
    private val repository = DlmsParserRepositoryImpl()
    private val parseDlmsDataUseCase = ParseDlmsDataUseCase(repository)
    private val parseMultipleDlmsDataUseCase = ParseMultipleDlmsDataUseCase(repository)
    private val validateHexDataUseCase = ValidateHexDataUseCase(repository)
    
    companion object {
        const val VALID_AARQ_HEX = "60 3A A1 09 06 07 60 85 74 05 08 01 01 A6 02 04 00 8A 02 07 80 8B 07 60 85 74 05 08 02 01 AC 0A 80 08 30 30 30 30 30 30 30 33 BE 10 04 0E 01 00 00 00 06 5F 1F 04 00 20 7E 1F 01 F4"
        const val VALID_AARE_HEX = "61 29 A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 00 BE 10 04 0E 08 00 06 5F 1F 04 00 00 1E 19 04 C8 00 07"
        const val INVALID_HEX = "INVALID_HEX_DATA"
    }
    
    @Test
    fun testValidateHexDataUseCase() {
        assertTrue(validateHexDataUseCase.execute(VALID_AARQ_HEX))
        assertTrue(validateHexDataUseCase.execute("60 3A A1 09"))
        assertFalse(validateHexDataUseCase.execute(INVALID_HEX))
        assertFalse(validateHexDataUseCase.execute("60 3A A1 0"))
    }
    
    @Test
    fun testParseDlmsDataUseCaseSuccess() = runTest {
        val result = parseDlmsDataUseCase.execute(VALID_AARQ_HEX)
        assertTrue(result is ParseResult.Success)
    }
    
    @Test
    fun testParseDlmsDataUseCaseInvalidHex() = runTest {
        val result = parseDlmsDataUseCase.execute(INVALID_HEX)
        assertTrue(result is ParseResult.Error)
        assertTrue(result.message.contains("Invalid hex format"))
    }
    
    @Test 
    fun testParseMultipleDlmsDataUseCaseSuccess() = runTest {
        val hexDataList = listOf(VALID_AARQ_HEX, VALID_AARE_HEX)
        val result = parseMultipleDlmsDataUseCase.execute(hexDataList)
        assertTrue(result is ParseResult.Success)
        assertTrue(result.data.size == 2)
    }
    
    @Test
    fun testParseMultipleDlmsDataUseCaseWithInvalidData() = runTest {
        val hexDataList = listOf(VALID_AARQ_HEX, INVALID_HEX, VALID_AARE_HEX)
        val result = parseMultipleDlmsDataUseCase.execute(hexDataList)
        assertTrue(result is ParseResult.Error)
        assertTrue(result.message.contains("Invalid hex format"))
    }
    
    @Test
    fun testParseMultipleDlmsDataUseCaseEmptyList() = runTest {
        val result = parseMultipleDlmsDataUseCase.execute(emptyList())
        assertTrue(result is ParseResult.Success)
        assertTrue(result.data.isEmpty())
    }
}