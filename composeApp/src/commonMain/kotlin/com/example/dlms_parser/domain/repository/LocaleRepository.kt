package com.example.dlms_parser.domain.repository

import com.example.dlms_parser.domain.model.Language

interface LocaleRepository {
    fun setLocale(language: Language)
}