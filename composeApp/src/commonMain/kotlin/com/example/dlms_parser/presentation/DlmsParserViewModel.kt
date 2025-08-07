package com.example.dlms_parser.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dlms_parser.domain.model.DlmsMessage
import com.example.dlms_parser.domain.model.ParseHistory
import com.example.dlms_parser.domain.model.ParseResult
import com.example.dlms_parser.domain.model.Language
import com.example.dlms_parser.domain.model.ViewFormat
import com.example.dlms_parser.domain.usecase.ExportMessagesUseCase
import com.example.dlms_parser.domain.usecase.HistoryUseCase
import com.example.dlms_parser.domain.usecase.ParseDlmsDataUseCase
import com.example.dlms_parser.domain.usecase.ParseMultipleDlmsDataUseCase
import com.example.dlms_parser.domain.usecase.ValidateHexDataUseCase
import com.example.dlms_parser.domain.repository.LocaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DlmsParserState(
    val isLoading: Boolean = false,
    val inputText: String = "",
    val parsedMessages: List<DlmsMessage> = emptyList(),
    val errorMessage: String? = null,
    val isInputValid: Boolean = true,
    val history: ParseHistory = ParseHistory(),
    val showHistory: Boolean = false,
    val exportStatus: String? = null,
    val isDarkTheme: Boolean = false,
    val currentLanguage: Language = Language.ENGLISH,
    val selectedFormat: ViewFormat = ViewFormat.JSON
)

class DlmsParserViewModel(
    private val parseDlmsDataUseCase: ParseDlmsDataUseCase,
    private val parseMultipleDlmsDataUseCase: ParseMultipleDlmsDataUseCase,
    private val validateHexDataUseCase: ValidateHexDataUseCase,
    private val historyUseCase: HistoryUseCase,
    private val exportMessagesUseCase: ExportMessagesUseCase,
    private val themePreferencesRepository: com.example.dlms_parser.data.repository.ThemePreferencesRepository,
    private val localeRepository: LocaleRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(DlmsParserState())
    val state: StateFlow<DlmsParserState> = _state.asStateFlow()
    
    init {
        // Load history and theme preference on initialization
        viewModelScope.launch {
            historyUseCase.getHistory().collect { history ->
                _state.value = _state.value.copy(history = history)
            }
        }
        
        viewModelScope.launch {
            themePreferencesRepository.isDarkTheme().collect { isDark ->
                _state.value = _state.value.copy(isDarkTheme = isDark)
            }
        }
        
        viewModelScope.launch {
            themePreferencesRepository.getLanguage().collect { language ->
                _state.value = _state.value.copy(currentLanguage = language)
                // Apply locale immediately when language changes
                localeRepository.setLocale(language)
            }
        }
        
    }
    
    fun updateInputText(text: String) {
        val isValid = if (text.isBlank()) {
            true
        } else {
            // Validate each non-empty line separately for multiple messages
            val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
            lines.all { validateHexDataUseCase.execute(it) }
        }
        
        _state.value = _state.value.copy(
            inputText = text,
            isInputValid = isValid,
            errorMessage = if (!isValid) "Invalid hex format in one or more lines" else null
        )
    }
    
    fun parseSingleMessage() {
        val input = _state.value.inputText.trim()
        if (input.isBlank()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, exportStatus = null)
            
            when (val result = parseDlmsDataUseCase.execute(input)) {
                is ParseResult.Success -> {
                    val messages = listOf(result.data)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        parsedMessages = messages,
                        errorMessage = null
                    )
                    // Save to history
                    historyUseCase.saveParseResult(input, messages, true)
                }
                is ParseResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    // Save error to history
                    historyUseCase.saveParseResult(input, emptyList(), false, result.message)
                }
            }
        }
    }
    
    fun parseMultipleMessages() {
        val input = _state.value.inputText.trim()
        if (input.isBlank()) return
        
        val lines = input.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
        
        if (lines.isEmpty()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, exportStatus = null)
            
            when (val result = parseMultipleDlmsDataUseCase.execute(lines)) {
                is ParseResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        parsedMessages = result.data,
                        errorMessage = null
                    )
                    // Save to history
                    historyUseCase.saveParseResult(input, result.data, true)
                }
                is ParseResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    // Save error to history
                    historyUseCase.saveParseResult(input, emptyList(), false, result.message)
                }
            }
        }
    }
    
    fun clearResults() {
        _state.value = _state.value.copy(
            parsedMessages = emptyList(),
            errorMessage = null,
            exportStatus = null
        )
    }
    
    fun toggleHistory() {
        _state.value = _state.value.copy(
            showHistory = !_state.value.showHistory
        )
    }
    
    fun loadFromHistory(entry: com.example.dlms_parser.domain.model.ParseHistoryEntry) {
        _state.value = _state.value.copy(
            inputText = entry.inputData,
            parsedMessages = entry.messages,
            errorMessage = if (entry.success) null else entry.errorMessage,
            showHistory = false
        )
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            historyUseCase.clearHistory()
        }
    }
    
    fun exportAsJson() {
        val messages = _state.value.parsedMessages
        val format = _state.value.selectedFormat
        if (messages.isEmpty()) return
        
        viewModelScope.launch {
            when (val result = exportMessagesUseCase.saveToFileInFormat(messages, format)) {
                is ParseResult.Success -> {
                    _state.value = _state.value.copy(
                        exportStatus = "Exported to: ${result.data}"
                    )
                }
                is ParseResult.Error -> {
                    _state.value = _state.value.copy(
                        exportStatus = "Export failed: ${result.message}"
                    )
                }
            }
        }
    }
    
    fun copyAsJson() {
        val messages = _state.value.parsedMessages
        val format = _state.value.selectedFormat
        if (messages.isEmpty()) return
        
        viewModelScope.launch {
            when (val result = exportMessagesUseCase.copyToClipboardInFormat(messages, format)) {
                is ParseResult.Success -> {
                    _state.value = _state.value.copy(
                        exportStatus = "Copied to clipboard as ${format.getDisplayName()}"
                    )
                }
                is ParseResult.Error -> {
                    _state.value = _state.value.copy(
                        exportStatus = "Copy failed: ${result.message}"
                    )
                }
            }
        }
    }
    
    fun copyText(text: String) {
        viewModelScope.launch {
            when (val result = exportMessagesUseCase.copyTextToClipboard(text)) {
                is ParseResult.Success -> {
                    _state.value = _state.value.copy(
                        exportStatus = "Copied to clipboard"
                    )
                }
                is ParseResult.Error -> {
                    _state.value = _state.value.copy(
                        exportStatus = "Copy failed: ${result.message}"
                    )
                }
            }
        }
    }
    
    fun clearExportStatus() {
        _state.value = _state.value.copy(exportStatus = null)
    }
    
    fun copyMessageAsJson(message: DlmsMessage) {
        val format = _state.value.selectedFormat
        viewModelScope.launch {
            when (val result = exportMessagesUseCase.copyToClipboardInFormat(listOf(message), format)) {
                is ParseResult.Success -> {
                    _state.value = _state.value.copy(
                        exportStatus = "Message copied to clipboard as ${format.getDisplayName()}"
                    )
                }
                is ParseResult.Error -> {
                    _state.value = _state.value.copy(
                        exportStatus = "Copy failed: ${result.message}"
                    )
                }
            }
        }
    }
    
    fun exportMessageAsJson(message: DlmsMessage) {
        val format = _state.value.selectedFormat
        val fileName = when (format) {
            ViewFormat.JSON -> "dlms_message.json"
            ViewFormat.XML -> "dlms_message.xml"
        }
        
        viewModelScope.launch {
            when (val result = exportMessagesUseCase.saveToFileInFormat(listOf(message), format, fileName)) {
                is ParseResult.Success -> {
                    _state.value = _state.value.copy(
                        exportStatus = "Message exported to: ${result.data}"
                    )
                }
                is ParseResult.Error -> {
                    _state.value = _state.value.copy(
                        exportStatus = "Export failed: ${result.message}"
                    )
                }
            }
        }
    }
    
    fun loadExampleData() {
        val exampleData = """
60 3A A1 09 06 07 60 85 74 05 08 01 01 A6 02 04 00 8A 02 07 80 8B 07 60 85 74 05 08 02 01 AC 0A 80 08 30 30 30 30 30 30 30 33 BE 10 04 0E 01 00 00 00 06 5F 1F 04 00 20 7E 1F 01 F4

61 29 A1 09 06 07 60 85 74 05 08 01 01 A2 03 02 01 00 A3 05 A1 03 02 01 00 BE 10 04 0E 08 00 06 5F 1F 04 00 00 1E 19 04 C8 00 07

C0 01 4F 00 01 00 00 81 00 00 00 02 00

C4 01 4F 00 09 1E 00 0C C3 13 02 07 50 14 14 14 00 80 10 00 49 00 1D 08 26 C8 10 00 00 0B 02 00 00 00 02 EE

C3 01 42 00 12 00 00 2C 00 00 FF 01 01 02 02 09 1E 4B 46 4D 41 44 32 31 4E 5F 45 56 4E 5F 42 47 5F 32 4B 5F 38 4D 5F 44 53 5F 76 39 30 30 39 06 00 05 A0 B2

C7 01 42 0C 00
        """.trimIndent()
        
        updateInputText(exampleData)
    }
    
    fun toggleTheme() {
        viewModelScope.launch {
            themePreferencesRepository.setDarkTheme(!_state.value.isDarkTheme)
        }
    }
    
    fun setLanguage(language: Language) {
        viewModelScope.launch {
            themePreferencesRepository.setLanguage(language)
            localeRepository.setLocale(language)
        }
    }
    
    fun setViewFormat(format: ViewFormat) {
        _state.value = _state.value.copy(selectedFormat = format)
    }
}