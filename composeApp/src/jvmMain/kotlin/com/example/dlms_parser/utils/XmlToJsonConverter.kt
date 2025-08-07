package com.example.dlms_parser.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.json.JSONObject

/**
 * Utility class for converting XML to formatted JSON using Jackson
 */
object XmlToJsonConverter {
    
    private val xmlMapper = XmlMapper()
    private val objectMapper = ObjectMapper()
    
    /**
     * Converts XML string to formatted JSON string using Jackson
     * @param xml The XML string to convert
     * @return Formatted JSON string with proper indentation
     */
    fun convertXmlToJson(xml: String): String {
        return try {
            if (xml.isBlank()) {
                "{}"
            } else {
                // Try Jackson XML to JSON conversion first
                convertWithJackson(xml)
            }
        } catch (e: Exception) {
            // Fallback to old method if Jackson fails
            fallbackConversion(xml, e)
        }
    }
    
    private fun convertWithJackson(xml: String): String {
        // Preprocess XML to fix common issues
        val cleanedXml = preprocessXmlForJsonConversion(xml)
        
        // Parse XML to JsonNode with Jackson
        val xmlNode: JsonNode = xmlMapper.readTree(cleanedXml)
        
        // Convert to formatted JSON
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlNode)
    }
    
    /**
     * Preprocesses XML to fix common structural issues
     * @param xml Original XML string
     * @return Cleaned XML string
     */
    private fun preprocessXmlForJsonConversion(xml: String): String {
        var cleanedXml = xml.replace("\r\n", "\n").replace("\r", "\n")
        
        // Fix ActionRequest duplicate tag issue
        // Original: <ActionRequest><ActionRequestNormal><ActionRequest>...</ActionRequest></ActionRequestNormal></ActionRequest>
        // Target: <ActionRequest><ActionRequestNormal>...</ActionRequestNormal></ActionRequest>
        
        if (cleanedXml.contains("<ActionRequest>") && cleanedXml.contains("<ActionRequestNormal>")) {
            // Remove the entire inner <ActionRequest>...</ActionRequest> block within ActionRequestNormal
            val pattern = Regex(
                "(<ActionRequestNormal>\\s*)<ActionRequest>(.*?)</ActionRequest>(\\s*</ActionRequestNormal>)", 
                RegexOption.DOT_MATCHES_ALL
            )
            cleanedXml = pattern.replace(cleanedXml) { matchResult ->
                "${matchResult.groupValues[1]}${matchResult.groupValues[2]}${matchResult.groupValues[3]}"
            }
        }
        
        // Similar fix for ActionResponse
        if (cleanedXml.contains("<ActionResponse>") && cleanedXml.contains("<ActionResponseNormal>")) {
            val pattern = Regex(
                "(<ActionResponseNormal>\\s*)<ActionResponse>(.*?)</ActionResponse>(\\s*</ActionResponseNormal>)", 
                RegexOption.DOT_MATCHES_ALL
            )
            cleanedXml = pattern.replace(cleanedXml) { matchResult ->
                "${matchResult.groupValues[1]}${matchResult.groupValues[2]}${matchResult.groupValues[3]}"
            }
        }
        
        return cleanedXml
    }
    
    private fun fallbackConversion(xml: String, originalException: Exception): String {
        return try {
            // Try with original org.json method as fallback
            val cleanedXml = preprocessXmlForJsonConversion(xml)
            val jsonObject = org.json.XML.toJSONObject(cleanedXml)
            jsonObject.toString(2)
        } catch (fallbackException: Exception) {
            // If all else fails, return error information
            JSONObject().apply {
                put("error", "Failed to convert XML to JSON")
                put("jacksonError", originalException.message ?: "Unknown Jackson error")
                put("fallbackError", fallbackException.message ?: "Unknown fallback error")
                put("originalXml", xml.replace("\r\n", "\n").replace("\r", "\n"))
            }.toString(2)
        }
    }
    
    /**
     * Formats JSON string with proper indentation using Jackson
     * @param json The JSON string to format
     * @return Formatted JSON string
     */
    fun formatJson(json: String): String {
        return try {
            val jsonNode = objectMapper.readTree(json)
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
        } catch (e: Exception) {
            json // Return as is if parsing fails
        }
    }
    
    /**
     * Minifies JSON string (removes whitespace and formatting) using Jackson
     * @param json The JSON string to minify
     * @return Minified JSON string
     */
    fun minifyJson(json: String): String {
        return try {
            val jsonNode = objectMapper.readTree(json)
            objectMapper.writeValueAsString(jsonNode)
        } catch (e: Exception) {
            json // Return as is if parsing fails
        }
    }
}