package com.example.dlms_parser

import com.example.dlms_parser.data.parser.GuruxDlmsParser
import com.example.dlms_parser.domain.model.ParseResult
import com.example.dlms_parser.domain.model.ActionRequestMessage

fun main() {
    val parser = GuruxDlmsParser()
    
    val actionRequestHex = "C3 01 42 00 12 00 00 2C 00 00 FF 01 01 02 02 09 1E 4B 46 4D 41 44 32 31 4E 5F 45 56 4E 5F 42 47 5F 32 4B 5F 38 4D 5F 44 53 5F 76 39 30 30 39 06 00 05 A0 B2"
    
    val result = parser.parseHexData(actionRequestHex)
    
    if (result is ParseResult.Success && result.data is ActionRequestMessage) {
        println("=".repeat(60))
        println("ActionRequest Message parsed successfully!")
        println("=".repeat(60))
        println("Raw Data: ${result.data.rawData}")
        println("Type: ${result.data.type}")
        println("Invoke ID: 0x${result.data.invokeId.toString(16).uppercase()}")
        println("Method: ${result.data.method}")
        println("Parameters count: ${result.data.parameters.structure.size}")
        println()
        println("JSON Structure:")
        println("-".repeat(40))
        println(result.data.xmlStructure)
        println("-".repeat(40))
    } else {
        println("Failed to parse or wrong type: $result")
    }
}