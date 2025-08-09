package com.example.dlms_parser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dlms_parser.domain.model.ViewFormat

@Composable
fun StructureViewer(
    json: String,
    originalXml: String? = null,
    globalFormat: ViewFormat = ViewFormat.JSON,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { isExpanded = !isExpanded }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "XML Structure (${globalFormat.getDisplayName()})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "" else "",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            if (isExpanded) {
                SelectionContainer {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .verticalScroll(scrollState)
                            .horizontalScroll(horizontalScrollState)
                            .padding(16.dp)
                    ) {
                        val displayContent = when (globalFormat) {
                            ViewFormat.JSON -> json
                            ViewFormat.XML -> originalXml ?: json
                        }
                        
                        Text(
                            text = buildAnnotatedString { 
                                when (globalFormat) {
                                    ViewFormat.JSON -> appendJsonWithSyntaxHighlighting(displayContent)
                                    ViewFormat.XML -> appendXmlWithSyntaxHighlighting(displayContent)
                                }
                            },
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun AnnotatedString.Builder.appendXmlWithSyntaxHighlighting(xml: String) {
    var i = 0
    while (i < xml.length) {
        val char = xml[i]
        
        when {
            char == '<' && i + 1 < xml.length && xml[i + 1] == '!' -> {
                val start = i
                var end = i + 4
                while (end < xml.length - 2 && xml.substring(end, end + 3) != "-->") {
                    end++
                }
                if (end < xml.length - 2) {
                    end += 3
                }
                
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF9E9E9E),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                ) {
                    append(xml.substring(start, end))
                }
                i = end
            }
            
            char == '<' -> {
                val start = i
                i++
                
                while (i < xml.length && xml[i] != '>') {
                    i++
                }
                if (i < xml.length) i++
                
                val tagContent = xml.substring(start, i)
                
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(tagContent)
                }
            }
            
            else -> {
                append(char)
                i++
            }
        }
    }
}

@Composable
fun JsonViewer(
    json: String,
    modifier: Modifier = Modifier
) {
    StructureViewer(
        json = json,
        originalXml = null,
        modifier = modifier
    )
}

private fun AnnotatedString.Builder.appendJsonWithSyntaxHighlighting(json: String) {
    var i = 0
    while (i < json.length) {
        val char = json[i]
        
        when (char) {
            '"' -> {
                val start = i
                i++
                
                while (i < json.length) {
                    if (json[i] == '"' && (i == 0 || json[i-1] != '\\')) {
                        i++
                        break
                    }
                    i++
                }
                
                val stringValue = json.substring(start, i)
                
                val isKey = start > 0 && json.substring(0, start).trimEnd().endsWith('{') ||
                           json.substring(0, start).trimEnd().endsWith(',')
                
                withStyle(
                    style = SpanStyle(
                        color = if (isKey) Color(0xFF9C27B0) else Color(0xFF4CAF50),
                        fontWeight = if (isKey) FontWeight.Medium else FontWeight.Normal
                    )
                ) {
                    append(stringValue)
                }
            }
            
            '{', '}', '[', ']' -> {
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(char)
                }
                i++
            }
            
            ':', ',' -> {
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF607D8B)
                    )
                ) {
                    append(char)
                }
                i++
            }
            
            in '0'..'9', '-', '.' -> {
                val start = i
                while (i < json.length && (json[i].isDigit() || json[i] in "-.")) {
                    i++
                }
                
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFFFF5722)
                    )
                ) {
                    append(json.substring(start, i))
                }
            }
            
            't', 'f', 'n' -> {
                val remaining = json.substring(i)
                when {
                    remaining.startsWith("true") -> {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("true")
                        }
                        i += 4
                    }
                    remaining.startsWith("false") -> {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFF44336),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("false")
                        }
                        i += 5
                    }
                    remaining.startsWith("null") -> {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF9E9E9E),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("null")
                        }
                        i += 4
                    }
                    else -> {
                        append(char)
                        i++
                    }
                }
            }
            
            else -> {
                append(char)
                i++
            }
        }
    }
}