package com.example.dlms_parser.domain.model

enum class ViewFormat {
    JSON,
    XML;
    
    fun getDisplayName(): String {
        return when (this) {
            JSON -> "JSON"
            XML -> "XML"
        }
    }
}