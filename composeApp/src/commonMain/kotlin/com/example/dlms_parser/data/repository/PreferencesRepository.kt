package com.example.dlms_parser.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.dlms_parser.domain.model.Language

class ThemePreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    
    fun isDarkTheme(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY] ?: false
    }
    
    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDark
        }
    }
    
    fun getLanguage(): Flow<Language> = dataStore.data.map { preferences ->
        val code = preferences[LANGUAGE_KEY] ?: Language.ENGLISH.code
        Language.fromCode(code)
    }
    
    suspend fun setLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }
    }
}