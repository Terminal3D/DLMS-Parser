package com.example.dlms_parser

import gurux.dlms.GXDLMSTranslator
import gurux.common.GXCommon

fun main() {
    val translator = GXDLMSTranslator()
    
    val testData = mapOf(
        "AARQ" to "60 3A A1 09 06 07 60 85 74 05 08 01 01 A6 02 04 00 8A 02 07 80 8B 07 60 85 74 05 08 02 01 AC 0A 80 08 30 30 30 30 30 30 30 33 BE 10 04 0E 01 00 00 00 06 5F 1F 04 00 20 7E 1F 01 F4",
        "AARE" to "61 29 A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 00 BE 10 04 0E 08 00 06 5F 1F 04 00 00 1E 19 04 C8 00 07",
        "GetRequest" to "C0 01 4F 00 01 00 00 81 00 00 00 02 00",
        "GetResponse" to "C4 01 4F 00 09 1E 00 0C C3 13 02 07 50 14 14 14 00 80 10 00 49 00 1D 08 26 C8 10 00 00 0B 02 00 00 00 02 EE",
        "ActionRequest" to "C3 01 42 00 12 00 00 2C 00 00 FF 01 01 02 02 09 1E 4B 46 4D 41 44 32 31 4E 5F 45 56 4E 5F 42 47 5F 32 4B 5F 38 4D 5F 44 53 5F 76 39 30 30 39 06 00 05 A0 B2",
        "ActionResponse" to "C7 01 42 0C 00"
    )
    
    testData.forEach { (type, hex) ->
        println("\n" + "=".repeat(50))
        println("Testing $type")
        println("=".repeat(50))
        println("HEX: $hex")
        println()
        
        try {
            val cleanHex = hex.replace(" ", "")
            val data = GXCommon.hexToBytes(cleanHex)
            val xml = translator.pduToXml(data)
            
            println("XML Output:")
            println(xml)
            println()
            
        } catch (e: Exception) {
            println("Error parsing $type: ${e.message}")
        }
    }
}