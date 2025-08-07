package com.example.dlms_parser.util

import com.example.dlms_parser.domain.model.Language
import com.example.dlms_parser.domain.repository.LocaleRepository
import java.util.Locale

class LocaleRepositoryImpl : LocaleRepository {
    override fun setLocale(language: Language) {
        val locale = when (language) {
            Language.ENGLISH -> Locale.ENGLISH
            Language.RUSSIAN -> Locale("ru")
            Language.ROMANIAN -> Locale("ro")
        }
        Locale.setDefault(locale)
    }
}