package com.example.dlms_parser.data.parser

import com.example.dlms_parser.domain.model.*
import com.example.dlms_parser.utils.XmlToJsonConverter
import gurux.dlms.GXDLMSTranslator
import gurux.common.GXCommon


class GuruxDlmsParser {
    
    private val translator = GXDLMSTranslator()
    
    companion object {
        // Use localized values enum for consistency  
        private val MISSING_VALUE = LocalizedValues.MISSING_VALUE.getDefaultValue()
        private val NOT_AVAILABLE = LocalizedValues.NOT_AVAILABLE.getDefaultValue() 
        private val UNKNOWN = LocalizedValues.UNKNOWN_VALUE.getDefaultValue()
    }
    
    fun parseHexData(hexData: String): ParseResult<DlmsMessage> {
        return try {
            val cleanHex = hexData.replace(" ", "").uppercase()
            if (cleanHex.isEmpty()) {
                return ParseResult.Error("Empty hex data")
            }
            
            val data = GXCommon.hexToBytes(cleanHex)
            if (data.isEmpty()) {
                return ParseResult.Error("Invalid hex data")
            }
            
            // Determine DLMS command type from the first byte
            val command = detectCommand(data[0])
            
            when (command) {
                0x60 -> parseAarq(cleanHex, data)
                0x61 -> parseAare(cleanHex, data) 
                0xC0 -> parseGetRequest(cleanHex, data)
                0xC4 -> parseGetResponse(cleanHex, data)
                0xC3 -> parseActionRequest(cleanHex, data)
                0xC7 -> parseActionResponse(cleanHex, data)
                0xC1 -> parseSetRequest(cleanHex, data)
                0xC5 -> parseSetResponse(cleanHex, data)
                0x05 -> parseReadRequest(cleanHex, data)
                0x0C -> parseReadResponse(cleanHex, data)
                0x06 -> parseWriteRequest(cleanHex, data)
                0x0D -> parseWriteResponse(cleanHex, data)
                else -> ParseResult.Error("Unsupported DLMS command: 0x${command.toString(16).uppercase()}")
            }
            
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse DLMS data: ${e.message}")
        }
    }
    
    private fun detectCommand(firstByte: Byte): Int {
        return firstByte.toUByte().toInt()
    }
    
    private fun parseAarq(hexData: String, data: ByteArray): ParseResult<AarqMessage> {
        return try {
            // Use Gurux translator to parse AARQ to XML
            val xml = translator.pduToXml(data)
            val jsonStructure = XmlToJsonConverter.convertXmlToJson(xml)
            
            val message = AarqMessage(
                rawData = hexData,
                xmlStructure = jsonStructure,
                originalXml = xml,
                applicationContextName = convertApplicationContextName(extractValueFromXml(xml, "ApplicationContextName")),
                senderAcseRequirements = extractValueFromXml(xml, "SenderACSERequirements") == "1",
                mechanismName = convertMechanismName(extractValueFromXml(xml, "MechanismName")),
                callingAuthenticationValue = extractValueFromXml(xml, "CallingAuthentication") ?: NOT_AVAILABLE,
                userInformation = extractValueFromXml(xml, "UserInformation") ?: NOT_AVAILABLE,
                initiateRequest = InitiateRequest(
                    responseAllowed = true, // AARQ always expects response
                    proposedDlmsVersionNumber = extractValueFromXml(xml, "ProposedDlmsVersionNumber")?.toInt(16) ?: 0,
                    proposedConformance = extractConformanceBitsFromXml(xml),
                    clientMaxReceivePduSize = extractValueFromXml(xml, "ProposedMaxPduSize")?.toInt(16) ?: 0
                )
            )
            
            ParseResult.Success(message)
            
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse AARQ: ${e.message}")
        }
    }
    
    private fun parseAare(hexData: String, data: ByteArray): ParseResult<AareMessage> {
        return try {
            val xml = translator.pduToXml(data)
            val jsonStructure = XmlToJsonConverter.convertXmlToJson(xml)
            
            val message = AareMessage(
                rawData = hexData,
                xmlStructure = jsonStructure,
                originalXml = xml,
                applicationContextName = convertApplicationContextName(extractValueFromXml(xml, "ApplicationContextName")),
                associationResult = parseAssociationResultFromXml(xml),
                resultSourceDiagnostic = extractResultSourceDiagnosticFromXml(xml),
                userInformation = extractValueFromXml(xml, "UserInformation") ?: NOT_AVAILABLE,
                initiateResponse = InitiateResponse(
                    negotiatedDlmsVersionNumber = extractValueFromXml(xml, "NegotiatedDlmsVersionNumber")?.toInt(16) ?: 0,
                    negotiatedConformance = extractNegotiatedConformanceFromXml(xml),
                    serverMaxReceivePduSize = extractValueFromXml(xml, "NegotiatedMaxPduSize")?.toInt(16) ?: 0,
                    vaaName = extractValueFromXml(xml, "VaaName")?.toInt(16) ?: 0
                )
            )
            
            ParseResult.Success(message)
            
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse AARE: ${e.message}")
        }
    }
    
    private fun parseGetRequest(hexData: String, data: ByteArray): ParseResult<GetRequestMessage> {
        return try {
            // Use Gurux translator to parse the request
            val xml = translator.pduToXml(data)
            val jsonStructure = XmlToJsonConverter.convertXmlToJson(xml)

            // Extract invoke ID from PDU (third byte in DLMS GetRequest: command + type + invokeID)
            val invokeId = if (data.size > 2) data[2].toUByte().toInt() else 0
            
            val message = GetRequestMessage(
                rawData = hexData,
                xmlStructure = jsonStructure,
                originalXml = xml,
                requestType = RequestType.NORMAL,
                invokeId = invokeId,
                attribute = extractAttributeFromXml(xml) ?: NOT_AVAILABLE
            )
            
            ParseResult.Success(message)
            
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse GetRequest: ${e.message}")
        }
    }
    
    private fun parseGetResponse(hexData: String, data: ByteArray): ParseResult<GetResponseMessage> {
        return try {
            val xml = translator.pduToXml(data)
            val jsonStructure = XmlToJsonConverter.convertXmlToJson(xml)
            val invokeId = if (data.size > 2) data[2].toUByte().toInt() else 0
            
            val message = GetResponseMessage(
                rawData = hexData,
                xmlStructure = jsonStructure,
                originalXml = xml,
                responseType = ResponseType.NORMAL,
                invokeId = invokeId,
                dataType = extractGetResponseDataType(xml),
                data = extractGetResponseData(xml)
            )
            
            ParseResult.Success(message)
            
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse GetResponse: ${e.message}")
        }
    }
    
    private fun parseActionRequest(hexData: String, data: ByteArray): ParseResult<ActionRequestMessage> {
        return try {
            val xml = translator.pduToXml(data)
            val jsonStructure = XmlToJsonConverter.convertXmlToJson(xml)
            val invokeId = if (data.size > 2) data[2].toUByte().toInt() else 0
            
            val message = ActionRequestMessage(
                rawData = hexData,
                xmlStructure = jsonStructure,
                originalXml = xml,
                requestType = RequestType.NORMAL,
                invokeId = invokeId,
                method = extractMethodFromXml(xml) ?: NOT_AVAILABLE,
                parameters = extractActionParametersFromXml(xml)
            )
            
            ParseResult.Success(message)
            
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse ActionRequest: ${e.message}")
        }
    }
    
    private fun parseActionResponse(hexData: String, data: ByteArray): ParseResult<ActionResponseMessage> {
        return try {
            val xml = translator.pduToXml(data)
            val jsonStructure = XmlToJsonConverter.convertXmlToJson(xml)
            val invokeId = if (data.size > 2) data[2].toUByte().toInt() else 0
            
            val message = ActionResponseMessage(
                rawData = hexData,
                xmlStructure = jsonStructure,
                originalXml = xml,
                responseType = ResponseType.NORMAL,
                invokeId = invokeId,
                actionResult = extractActionResultFromXml(xml)
            )
            
            ParseResult.Success(message)
            
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse ActionResponse: ${e.message}")
        }
    }
    
    // Additional DLMS commands support - create basic message classes for unsupported types
    private fun parseSetRequest(hexData: String, data: ByteArray): ParseResult<DlmsMessage> {
        return try {
            val xml = translator.pduToXml(data)
            val jsonStructure = XmlToJsonConverter.convertXmlToJson(xml)

            // Create basic SetRequest message
            val message = SetRequestMessage(rawData = hexData, xmlStructure = jsonStructure, originalXml = xml)
            
            ParseResult.Success(message)
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse SetRequest: ${e.message}")
        }
    }
    
    private fun parseSetResponse(hexData: String, data: ByteArray): ParseResult<DlmsMessage> {
        return try {
            val xml = translator.pduToXml(data)
            val jsonStructure = XmlToJsonConverter.convertXmlToJson(xml)
            val message = SetResponseMessage(rawData = hexData, xmlStructure = jsonStructure, originalXml = xml)
            ParseResult.Success(message)
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse SetResponse: ${e.message}")
        }
    }
    
    private fun parseReadRequest(hexData: String, data: ByteArray): ParseResult<DlmsMessage> {
        return try {
            val message = ReadRequestMessage(rawData = hexData, xmlStructure = null, originalXml = null)
            ParseResult.Success(message)
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse ReadRequest: ${e.message}")
        }
    }
    
    private fun parseReadResponse(hexData: String, data: ByteArray): ParseResult<DlmsMessage> {
        return try {
            val message = ReadResponseMessage(rawData = hexData, xmlStructure = null, originalXml = null)
            ParseResult.Success(message)
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse ReadResponse: ${e.message}")
        }
    }
    
    private fun parseWriteRequest(hexData: String, data: ByteArray): ParseResult<DlmsMessage> {
        return try {
            val message = WriteRequestMessage(rawData = hexData, xmlStructure = null, originalXml = null)
            ParseResult.Success(message)
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse WriteRequest: ${e.message}")
        }
    }
    
    private fun parseWriteResponse(hexData: String, data: ByteArray): ParseResult<DlmsMessage> {
        return try {
            val message = WriteResponseMessage(rawData = hexData, xmlStructure = null, originalXml = null)
            ParseResult.Success(message)
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse WriteResponse: ${e.message}")
        }
    }
    
    
    // Extract values from XML attributes (for Gurux XML format like <Tag Value="..." />)
    private fun extractValueFromXml(xml: String, tagName: String): String? {
        val pattern = "<$tagName[^>]*Value\\s*=\\s*\"([^\"]*)\"".toRegex(RegexOption.IGNORE_CASE)
        val match = pattern.find(xml)
        return match?.groupValues?.get(1)?.trim()
    }
    
    // Extract conformance bits from XML structure
    private fun extractConformanceBitsFromXml(xml: String): List<String> {
        val conformanceBits = mutableListOf<String>()
        val conformancePattern = "<ConformanceBit Name=\"([^\"]*)\">".toRegex(RegexOption.IGNORE_CASE)
        
        conformancePattern.findAll(xml).forEach { match ->
            conformanceBits.add(match.groupValues[1])
        }
        
        return conformanceBits
    }
    
    
    private fun extractAttributeFromXml(xml: String): String? {
        // GetRequest contains AttributeDescriptor with ClassId, InstanceId, AttributeId
        val classId = extractValueFromXml(xml, "ClassId")
        val instanceId = extractValueFromXml(xml, "InstanceId")  
        val attributeId = extractValueFromXml(xml, "AttributeId")
        
        if (classId != null && instanceId != null && attributeId != null) {
            // Convert to OBIS format: ClassId:A-B:C.D.E*AttributeId where InstanceId is A-B:C.D.E*F
            val classValue = classId.toInt(16)
            val attributeValue = attributeId.toInt(16)
            val obisCode = formatInstanceIdToObis(instanceId)
            return "$classValue:$obisCode:$attributeValue"
        }
        
        // Fallback to old methods
        return tryExtractFromXml(xml, listOf("AttributeDescriptor", "CosemAttributeDescriptor", "attribute"))
    }
    
    private fun extractDataTypeFromXml(xml: String): String? {
        return tryExtractFromXml(xml, listOf("DataType", "data-type", "type"))
    }
    
    private fun extractDataFromXml(xml: String): String? {
        return tryExtractFromXml(xml, listOf("Data", "data", "value"))
    }
    
    // Specific methods for GetResponse parsing from correct XML structure
    private fun extractGetResponseDataType(xml: String): String {
        // GetResponse structure: GetResponse/GetResponseNormal/Result/Data/{DataType}
        return when {
            xml.contains("<OctetString") -> "OCTET_STRING"
            xml.contains("<UInt32") -> "UINT32" 
            xml.contains("<UInt16") -> "UINT16"
            xml.contains("<UInt8") -> "UINT8"
            xml.contains("<Structure") -> "STRUCTURE"
            xml.contains("<Array") -> "ARRAY"
            xml.contains("<Boolean") -> "BOOLEAN"
            xml.contains("<Integer") -> "INTEGER"
            xml.contains("<DateTime") -> "DATETIME"
            xml.contains("<Date") -> "DATE"
            xml.contains("<Time") -> "TIME"
            else -> UNKNOWN
        }
    }
    
    private fun extractGetResponseData(xml: String): String {
        // GetResponse structure: GetResponse/GetResponseNormal/Result/Data/{DataType}/{Value}
        // Try different data types
        
        // OctetString
        val octetStringValue = extractValueFromXml(xml, "OctetString")
        if (octetStringValue != null) {
            return octetStringValue
        }
        
        // Numeric types
        val uint32Value = extractValueFromXml(xml, "UInt32")
        if (uint32Value != null) {
            return uint32Value
        }
        
        val uint16Value = extractValueFromXml(xml, "UInt16")
        if (uint16Value != null) {
            return uint16Value
        }
        
        val uint8Value = extractValueFromXml(xml, "UInt8")
        if (uint8Value != null) {
            return uint8Value
        }
        
        val integerValue = extractValueFromXml(xml, "Integer")
        if (integerValue != null) {
            return integerValue
        }
        
        // Boolean
        val booleanValue = extractValueFromXml(xml, "Boolean")
        if (booleanValue != null) {
            return booleanValue
        }
        
        // DateTime/Date/Time
        val dateTimeValue = extractValueFromXml(xml, "DateTime")
        if (dateTimeValue != null) {
            return dateTimeValue
        }
        
        val dateValue = extractValueFromXml(xml, "Date")
        if (dateValue != null) {
            return dateValue
        }
        
        val timeValue = extractValueFromXml(xml, "Time")
        if (timeValue != null) {
            return timeValue
        }
        
        return MISSING_VALUE
    }
    
    private fun extractMethodFromXml(xml: String): String? {
        // ActionRequest contains MethodDescriptor with ClassId, InstanceId, MethodId
        val classId = extractValueFromXml(xml, "ClassId")
        val instanceId = extractValueFromXml(xml, "InstanceId")
        val methodId = extractValueFromXml(xml, "MethodId")
        
        if (classId != null && instanceId != null && methodId != null) {
            // Convert to OBIS format: ClassId:A-B:C.D.E*F:G where InstanceId is A-B:C.D.E format
            val classValue = classId.toInt(16)
            val methodValue = methodId.toInt(16)
            val obisCode = formatInstanceIdToObis(instanceId)
            return "$classValue:$obisCode:$methodValue"
        }
        
        // Fallback to old methods
        return tryExtractFromXml(xml, listOf("MethodDescriptor", "method", "cosemMethodDescriptor"))
    }
    
    private fun extractActionParametersFromXml(xml: String): ActionParameters {
        val parametersXml = tryExtractFromXml(xml, listOf("MethodInvocationParameters", "parameters", "data"))
        
        return if (parametersXml != null) {
            // Try to parse actual parameters from XML
            ActionParameters(structure = parseStructureFromXml(parametersXml))
        } else {
            // Return empty structure when parameters are not available
            ActionParameters(structure = emptyList())
        }
    }
    
    private fun extractActionResultFromXml(xml: String): ActionResult {
        val resultText = tryExtractFromXml(xml, listOf("ActionResult", "result", "action-result"))?.lowercase()
        
        return when {
            resultText?.contains("success") == true || resultText == "0" -> ActionResult.SUCCESS
            resultText?.contains("hardware-fault") == true || resultText == "1" -> ActionResult.HARDWARE_FAULT
            resultText?.contains("temporary-failure") == true || resultText == "2" -> ActionResult.TEMPORARY_FAILURE
            resultText?.contains("read-write-denied") == true || resultText == "3" -> ActionResult.READ_WRITE_DENIED
            resultText?.contains("object-unavailable") == true || resultText == "4" -> ActionResult.OBJECT_UNAVAILABLE
            else -> ActionResult.TYPE_UNMATCHED
        }
    }
    
    private fun tryExtractFromXml(xml: String, tagNames: List<String>): String? {
        for (tagName in tagNames) {
            val pattern = "<$tagName[^>]*>(.*?)</$tagName>".toRegex(RegexOption.IGNORE_CASE)
            val match = pattern.find(xml)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return null
    }
    
    private fun parseStructureFromXml(xml: String): List<ActionParameter> {
        // Parse structure elements from XML using Value attributes (Gurux format)
        val elements = mutableListOf<ActionParameter>()
        
        // Look for common DLMS data types with Value attribute
        val octetStringPattern = "<OctetString[^>]*Value\\s*=\\s*\"([^\"]*)\"".toRegex(RegexOption.IGNORE_CASE)
        val uint32Pattern = "<UInt32[^>]*Value\\s*=\\s*\"([^\"]*)\"".toRegex(RegexOption.IGNORE_CASE)
        val uint16Pattern = "<UInt16[^>]*Value\\s*=\\s*\"([^\"]*)\"".toRegex(RegexOption.IGNORE_CASE)
        val unsignedPattern = "<Unsigned[^>]*Value\\s*=\\s*\"([^\"]*)\"".toRegex(RegexOption.IGNORE_CASE)
        
        // Extract OctetString values
        octetStringPattern.findAll(xml).forEach { match ->
            val hexValue = match.groupValues[1]
            // Convert hex to ASCII string if possible, otherwise keep hex
            val stringValue = try {
                hexValue.chunked(2).map { it.toInt(16).toChar() }.joinToString("")
            } catch (e: Exception) {
                hexValue
            }
            elements.add(ActionParameter.OctetString(stringValue))
        }
        
        // Extract UInt32 values  
        uint32Pattern.findAll(xml).forEach { match ->
            val hexValue = match.groupValues[1]
            val value = hexValue.toLong(16)
            elements.add(ActionParameter.DoubleLongUnsigned(value))
        }
        
        // Extract UInt16 values
        uint16Pattern.findAll(xml).forEach { match ->
            val hexValue = match.groupValues[1]
            val value = hexValue.toLong(16)
            elements.add(ActionParameter.DoubleLongUnsigned(value))
        }
        
        // Extract other Unsigned values
        unsignedPattern.findAll(xml).forEach { match ->
            val value = match.groupValues[1].toLongOrNull(16) ?: 0L
            elements.add(ActionParameter.DoubleLongUnsigned(value))
        }
        
        return elements
    }
    
    
    private fun extractNegotiatedConformanceFromXml(xml: String): List<String> {
        val conformanceBits = mutableListOf<String>()
        val conformancePattern = "<ConformanceBit Name=\"([^\"]*)\">".toRegex(RegexOption.IGNORE_CASE)
        
        // Look specifically in the NegotiatedConformance section
        val negotiatedSection = "<NegotiatedConformance>.*?</NegotiatedConformance>".toRegex(RegexOption.DOT_MATCHES_ALL).find(xml)?.value
        if (negotiatedSection != null) {
            conformancePattern.findAll(negotiatedSection).forEach { match ->
                conformanceBits.add(match.groupValues[1])
            }
        }
        
        return conformanceBits
    }
    
    private fun extractResultSourceDiagnosticFromXml(xml: String): String {
        // Look for ACSEServiceUser value in ResultSourceDiagnostic section
        val acseValue = extractValueFromXml(xml, "ACSEServiceUser")
        return acseValue ?: NOT_AVAILABLE
    }
    
    // Convert application context name from human readable to OID
    private fun convertApplicationContextName(contextName: String?): String {
        return when (contextName?.uppercase()) {
            "LN" -> "2.16.756.5.8.1.1"  // Logical Name referencing
            "SN" -> "2.16.756.5.8.1.2"  // Short Name referencing  
            else -> contextName ?: NOT_AVAILABLE
        }
    }
    
    // Convert mechanism name from human readable to OID
    private fun convertMechanismName(mechanismName: String?): String {
        return when (mechanismName?.uppercase()) {
            "LOW" -> "2.16.756.5.8.2.1"    // Low Level Security
            "HIGH" -> "2.16.756.5.8.2.2"   // High Level Security
            "HLS_MD5" -> "2.16.756.5.8.2.3"  // HLS MD5
            "HLS_SHA1" -> "2.16.756.5.8.2.4"  // HLS SHA1
            "HLS_GMAC" -> "2.16.756.5.8.2.5"  // HLS GMAC
            else -> mechanismName ?: NOT_AVAILABLE
        }
    }
    
    private fun parseAssociationResultFromXml(xml: String): com.example.dlms_parser.domain.model.AssociationResult {
        val resultValue = extractValueFromXml(xml, "AssociationResult")
        return when (resultValue) {
            "0", "00", "accepted" -> com.example.dlms_parser.domain.model.AssociationResult.ACCEPTED
            "1", "01", "rejected-permanent" -> com.example.dlms_parser.domain.model.AssociationResult.REJECTED_PERMANENT
            "2", "02", "rejected-transient" -> com.example.dlms_parser.domain.model.AssociationResult.REJECTED_TRANSIENT
            else -> com.example.dlms_parser.domain.model.AssociationResult.UNKNOWN
        }
    }
    
    /**
     * Converts hex InstanceId to OBIS code format (A-B:C.D.E*F)
     * InstanceId is usually 6 bytes: AA BB CC DD EE FF -> A-B:C.D.E*F
     */
    private fun formatInstanceIdToObis(instanceId: String): String {
        return try {
            // Remove any spaces and ensure even length
            val cleanHex = instanceId.replace(" ", "").uppercase()
            if (cleanHex.length == 12) { // 6 bytes = 12 hex chars
                val a = cleanHex.substring(0, 2).toInt(16)
                val b = cleanHex.substring(2, 4).toInt(16)  
                val c = cleanHex.substring(4, 6).toInt(16)
                val d = cleanHex.substring(6, 8).toInt(16)
                val e = cleanHex.substring(8, 10).toInt(16)
                val f = cleanHex.substring(10, 12).toInt(16)
                "$a-$b:$c.$d.$e*$f"
            } else {
                // If not 6 bytes, return as is
                instanceId
            }
        } catch (e: Exception) {
            // If parsing fails, return original
            instanceId
        }
    }
    
    fun validateHexFormat(hexData: String): Boolean {
        val cleanHex = hexData.replace(" ", "").uppercase()
        return cleanHex.matches(Regex("[0-9A-F]*")) && cleanHex.length % 2 == 0
    }
}