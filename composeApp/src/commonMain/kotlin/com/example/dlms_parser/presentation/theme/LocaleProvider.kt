package com.example.dlms_parser.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.example.dlms_parser.domain.model.Language

val LocalLanguage = compositionLocalOf { Language.ENGLISH }

@Composable
fun LanguageProvider(
    language: Language,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLanguage provides language,
        content = content
    )
}