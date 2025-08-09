package com.example.dlms_parser

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import com.example.dlms_parser.domain.model.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dlms_parser.presentation.DlmsParserViewModel
import com.example.dlms_parser.presentation.components.*
import com.example.dlms_parser.presentation.theme.DlmsParserTheme
import com.example.dlms_parser.presentation.theme.LanguageProvider
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import dlms_parser.composeapp.generated.resources.Res
import dlms_parser.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val viewModel: DlmsParserViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    DlmsParserTheme(darkTheme = state.isDarkTheme) {
        LanguageProvider(language = state.currentLanguage) {
            key(state.currentLanguage) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(if (state.showHistory) 0.7f else 1f)
                        .fillMaxHeight()
                ) {
                    TopAppBar(
                        title = { 
                            Text(
                                stringResource(Res.string.app_title),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        actions = {
                            LanguageDropdown(
                                currentLanguage = state.currentLanguage,
                                onLanguageSelected = viewModel::setLanguage
                            )
                            IconButton(onClick = viewModel::toggleTheme) {
                                Icon(
                                    if (state.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = stringResource(Res.string.content_description_theme_toggle),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = viewModel::toggleHistory) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = stringResource(Res.string.content_description_history),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    
                    StatusSection(
                        errorMessage = state.errorMessage,
                        exportStatus = state.exportStatus,
                        onClearExportStatus = viewModel::clearExportStatus,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InputSection(
                            state = state,
                            onInputChange = viewModel::updateInputText,
                            onParseMultiple = viewModel::parseMultipleMessages,
                            onLoadExamples = viewModel::loadExampleData,
                            onClearInput = { viewModel.updateInputText("") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (state.parsedMessages.isNotEmpty()) {
                            ResultsSection(
                                messages = state.parsedMessages,
                                selectedFormat = state.selectedFormat,
                                onFormatChange = viewModel::setViewFormat,
                                onClearResults = viewModel::clearResults,
                                onExportJson = viewModel::exportAsJson,
                                onCopyAsJson = viewModel::copyAsJson,
                                onCopyText = viewModel::copyText,
                                onCopyMessageAsJson = viewModel::copyMessageAsJson,
                                onExportMessageAsJson = viewModel::exportMessageAsJson,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                AnimatedVisibility(
                    visible = state.showHistory,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                ) {
                    HistorySection(
                        history = state.history,
                        onLoadFromHistory = viewModel::loadFromHistory,
                        onClearHistory = viewModel::clearHistory,
                        modifier = Modifier
                            .width(400.dp)
                            .fillMaxHeight()
                    )
                }
            }
                }
            }
        }
    }
}