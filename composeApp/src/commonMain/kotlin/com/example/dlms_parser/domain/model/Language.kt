package com.example.dlms_parser.domain.model

enum class Language(
    val code: String,
    val flag: String,
    val displayName: String
) {
    ENGLISH("en", "🇺🇸", "English"),
    RUSSIAN("ru", "🇷🇺", "Русский"),
    ROMANIAN("ro", "🇷🇴", "Română");

    fun toLocale(): String = code

    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: ENGLISH
        }
        
        fun getAll(): List<Language> = values().toList()
    }
}