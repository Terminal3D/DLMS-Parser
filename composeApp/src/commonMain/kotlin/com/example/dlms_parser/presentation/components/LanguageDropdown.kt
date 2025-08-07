package com.example.dlms_parser.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dlms_parser.domain.model.Language
import org.jetbrains.compose.resources.stringResource
import dlms_parser.composeapp.generated.resources.Res
import dlms_parser.composeapp.generated.resources.content_description_language_toggle

@Composable
fun LanguageDropdown(
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Default.Language,
                contentDescription = stringResource(Res.string.content_description_language_toggle),
                modifier = Modifier.size(24.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Language.getAll().forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = language.flag,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = language.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (language == currentLanguage) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    },
                    colors = if (language == currentLanguage) {
                        MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        MenuDefaults.itemColors()
                    }
                )
            }
        }
    }
}