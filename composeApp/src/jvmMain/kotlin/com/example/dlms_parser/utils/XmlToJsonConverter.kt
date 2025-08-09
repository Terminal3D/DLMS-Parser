package com.example.dlms_parser.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.json.JSONObject

object XmlToJsonConverter {
    
    private val xmlMapper = XmlMapper()
    private val objectMapper = ObjectMapper()
    
    fun convertXmlToJson(xml: String): String {
        return try {
            if (xml.isBlank()) {
                "{}"
            } else {
                convertWithJackson(xml)
            }
        } catch (e: Exception) {
            fallbackConversion(xml, e)
        }
    }
    
    private fun convertWithJackson(xml: String): String {
        val cleanedXml = preprocessXmlForJsonConversion(xml)
        
        val xmlNode: JsonNode = xmlMapper.readTree(cleanedXml)
        
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlNode)
    }
    
    private fun preprocessXmlForJsonConversion(xml: String): String {
        var cleanedXml = xml.replace("\r\n", "\n").replace("\r", "\n")
        
        
        if (cleanedXml.contains("<ActionRequest>") && cleanedXml.contains("<ActionRequestNormal>")) {
            val pattern = Regex(
                "(<ActionRequestNormal>\\s*)<ActionRequest>(.*?)</ActionRequest>(\\s*</ActionRequestNormal>)", 
                RegexOption.DOT_MATCHES_ALL
            )
            cleanedXml = pattern.replace(cleanedXml) { matchResult ->
                "${matchResult.groupValues[1]}${matchResult.groupValues[2]}${matchResult.groupValues[3]}"
            }
        }
        
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
            val cleanedXml = preprocessXmlForJsonConversion(xml)
            val jsonObject = org.json.XML.toJSONObject(cleanedXml)
            jsonObject.toString(2)
        } catch (fallbackException: Exception) {
            JSONObject().apply {
                put("error", "Failed to convert XML to JSON")
                put("jacksonError", originalException.message ?: "Unknown Jackson error")
                put("fallbackError", fallbackException.message ?: "Unknown fallback error")
                put("originalXml", xml.replace("\r\n", "\n").replace("\r", "\n"))
            }.toString(2)
        }
    }
    
    fun formatJson(json: String): String {
        return try {
            val jsonNode = objectMapper.readTree(json)
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
        } catch (e: Exception) {
            json
        }
    }
    
    fun minifyJson(json: String): String {
        return try {
            val jsonNode = objectMapper.readTree(json)
            objectMapper.writeValueAsString(jsonNode)
        } catch (e: Exception) {
            json
        }
    }
}