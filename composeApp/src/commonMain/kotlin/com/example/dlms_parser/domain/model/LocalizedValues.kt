package com.example.dlms_parser.domain.model

enum class LocalizedValues {
    MISSING_VALUE,
    NOT_AVAILABLE,
    UNKNOWN_VALUE;
    
    fun getDefaultValue(): String {
        return when (this) {
            MISSING_VALUE -> "Missing"
            NOT_AVAILABLE -> "N/A"
            UNKNOWN_VALUE -> "Unknown"
        }
    }
}