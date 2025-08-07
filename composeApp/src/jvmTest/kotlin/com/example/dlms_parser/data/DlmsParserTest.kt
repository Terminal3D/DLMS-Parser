package com.example.dlms_parser.data

import com.example.dlms_parser.data.parser.GuruxDlmsParser
import com.example.dlms_parser.domain.model.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DlmsParserTest {
    
    private val parser = GuruxDlmsParser()
    
    companion object {
        // Test data from the provided examples
        const val AARQ_HEX = "60 3A A1 09 06 07 60 85 74 05 08 01 01 A6 02 04 00 8A 02 07 80 8B 07 60 85 74 05 08 02 01 AC 0A 80 08 30 30 30 30 30 30 30 33 BE 10 04 0E 01 00 00 00 06 5F 1F 04 00 20 7E 1F 01 F4"
        
        const val AARE_HEX = "61 29 A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 00 BE 10 04 0E 08 00 06 5F 1F 04 00 00 1E 19 04 C8 00 07"
        
        const val GET_REQUEST_HEX = "C0 01 4F 00 01 00 00 81 00 00 00 02 00"
        
        const val GET_RESPONSE_HEX = "C4 01 4F 00 09 1E 00 0C C3 13 02 07 50 14 14 14 00 80 10 00 49 00 1D 08 26 C8 10 00 00 0B 02 00 00 00 02 EE"
        
        const val ACTION_REQUEST_HEX = "C3 01 42 00 12 00 00 2C 00 00 FF 01 01 02 02 09 1E 4B 46 4D 41 44 32 31 4E 5F 45 56 4E 5F 42 47 5F 32 4B 5F 38 4D 5F 44 53 5F 76 39 30 30 39 06 00 05 A0 B2"
        
        const val ACTION_RESPONSE_HEX = "C7 01 42 0C 00"
    }
    
    @Test
    fun testHexValidation() {
        assertTrue(parser.validateHexFormat("60 3A A1 09"))
        assertTrue(parser.validateHexFormat("603aa109"))
        assertTrue(parser.validateHexFormat(""))
        assertTrue(!parser.validateHexFormat("G0 3A A1 09"))
        assertTrue(!parser.validateHexFormat("60 3A A1 0"))
    }
    
    @Test
    fun testParseAarqMessage() {
        val result = parser.parseHexData(AARQ_HEX)
        
        assertTrue(result is ParseResult.Success)
        val message = result.getOrNull()
        assertNotNull(message)
        assertTrue(message is AarqMessage)
        
        val aarq = message as AarqMessage
        assertEquals("AARQ", aarq.type)
        assertEquals("2.16.756.5.8.1.1", aarq.applicationContextName)
        assertTrue(aarq.senderAcseRequirements)
        assertEquals("2.16.756.5.8.2.1", aarq.mechanismName)
        assertEquals(6, aarq.initiateRequest.proposedDlmsVersionNumber)
        assertEquals(500, aarq.initiateRequest.clientMaxReceivePduSize)
        assertTrue(aarq.initiateRequest.responseAllowed)
    }
    
    @Test
    fun testParseAareMessage() {
        val result = parser.parseHexData(AARE_HEX)
        
        assertTrue(result is ParseResult.Success)
        val message = result.getOrNull()
        assertNotNull(message)
        assertTrue(message is AareMessage)
        
        val aare = message as AareMessage
        assertEquals("AARE", aare.type)
        assertEquals("2.16.756.5.8.1.1", aare.applicationContextName)
        assertEquals(AssociationResult.ACCEPTED, aare.associationResult)
        assertEquals(6, aare.initiateResponse.negotiatedDlmsVersionNumber)
        assertEquals(1224, aare.initiateResponse.serverMaxReceivePduSize)
        assertEquals(7, aare.initiateResponse.vaaName)
    }
    
    @Test
    fun testParseGetRequestMessage() {
        val result = parser.parseHexData(GET_REQUEST_HEX)
        
        assertTrue(result is ParseResult.Success)
        val message = result.getOrNull()
        assertNotNull(message)
        assertTrue(message is GetRequestMessage)
        
        val getRequest = message as GetRequestMessage
        assertEquals("GetRequest", getRequest.type)
        assertEquals(RequestType.NORMAL, getRequest.requestType)
        assertEquals(0x4F, getRequest.invokeId)
        assertEquals("1:0-0:129.0.0*0:2", getRequest.attribute)
    }
    
    @Test
    fun testParseGetResponseMessage() {
        val result = parser.parseHexData(GET_RESPONSE_HEX)
        
        assertTrue(result is ParseResult.Success)
        val message = result.getOrNull()
        assertNotNull(message)
        assertTrue(message is GetResponseMessage)
        
        val getResponse = message as GetResponseMessage
        assertEquals("GetResponse", getResponse.type)
        assertEquals(ResponseType.NORMAL, getResponse.responseType)
        assertEquals(0x4F, getResponse.invokeId)
        assertEquals("OCTET_STRING", getResponse.dataType)
    }
    
    @Test
    fun testParseActionRequestMessage() {
        val result = parser.parseHexData(ACTION_REQUEST_HEX)
        
        assertTrue(result is ParseResult.Success)
        val message = result.getOrNull()
        assertNotNull(message)
        assertTrue(message is ActionRequestMessage)
        
        val actionRequest = message as ActionRequestMessage
        assertEquals("ActionRequest", actionRequest.type)
        assertEquals(RequestType.NORMAL, actionRequest.requestType)
        assertEquals(0x42, actionRequest.invokeId)
        assertEquals("18:0-0:44.0.0*255:1", actionRequest.method)
        assertTrue(actionRequest.parameters.structure.isNotEmpty())
    }
    
    @Test
    fun testParseActionResponseMessage() {
        val result = parser.parseHexData(ACTION_RESPONSE_HEX)
        
        assertTrue(result is ParseResult.Success)
        val message = result.getOrNull()
        assertNotNull(message)
        assertTrue(message is ActionResponseMessage)
        
        val actionResponse = message as ActionResponseMessage
        assertEquals("ActionResponse", actionResponse.type)
        assertEquals(ResponseType.NORMAL, actionResponse.responseType)
        assertEquals(0x42, actionResponse.invokeId)
        assertEquals(ActionResult.TYPE_UNMATCHED, actionResponse.actionResult)
    }
    
    @Test
    fun testParseInvalidHexData() {
        val result = parser.parseHexData("INVALID_HEX")
        
        assertTrue(result is ParseResult.Error)
        val error = result as ParseResult.Error
        assertNotNull(error.message)
    }
    
    @Test
    fun testParseAllExampleMessages() {
        val testData = listOf(
            AARQ_HEX to "AARQ",
            AARE_HEX to "AARE", 
            GET_REQUEST_HEX to "GetRequest",
            GET_RESPONSE_HEX to "GetResponse",
            ACTION_REQUEST_HEX to "ActionRequest",
            ACTION_RESPONSE_HEX to "ActionResponse"
        )
        
        testData.forEach { (hex, expectedType) ->
            val result = parser.parseHexData(hex)
            assertTrue("Failed to parse $expectedType") { result is ParseResult.Success }
            
            val message = result.getOrNull()
            assertNotNull(message, "Message should not be null for $expectedType")
            assertEquals(expectedType, message.type, "Message type should match for $expectedType")
        }
    }
}