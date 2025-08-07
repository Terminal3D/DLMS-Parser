package com.example.dlms_parser.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

/**
 * Компонент для отображения XML/JSON структуры с подсветкой синтаксиса (скрываемый)
 */
@Composable
fun StructureViewer(
    json: String,
    originalXml: String? = null,
    globalFormat: ViewFormat = ViewFormat.JSON,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) } // По умолчанию скрыто
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Заголовок с шевроном
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
                    contentDescription = if (isExpanded) "Скрыть структуру" else "Показать структуру",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Контент с подсветкой (только когда развернуто)
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

/**
 * Добавляет подсветку синтаксиса для XML строки
 */
private fun AnnotatedString.Builder.appendXmlWithSyntaxHighlighting(xml: String) {
    var i = 0
    while (i < xml.length) {
        val char = xml[i]
        
        when {
            char == '<' && i + 1 < xml.length && xml[i + 1] == '!' -> {
                // XML комментарии <!--...-->
                val start = i
                var end = i + 4
                while (end < xml.length - 2 && xml.substring(end, end + 3) != "-->") {
                    end++
                }
                if (end < xml.length - 2) {
                    end += 3 // Включаем -->
                }
                
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF9E9E9E), // Серый
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                ) {
                    append(xml.substring(start, end))
                }
                i = end
            }
            
            char == '<' -> {
                // XML теги
                val start = i
                i++ // Пропускаем <
                
                while (i < xml.length && xml[i] != '>') {
                    i++
                }
                if (i < xml.length) i++ // Включаем >
                
                val tagContent = xml.substring(start, i)
                
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF2196F3), // Синий
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(tagContent)
                }
            }
            
            else -> {
                // Остальные символы
                append(char)
                i++
            }
        }
    }
}

// Добавим совместимость для старого названия
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

/**
 * Добавляет подсветку синтаксиса для JSON строки
 */
private fun AnnotatedString.Builder.appendJsonWithSyntaxHighlighting(json: String) {
    var i = 0
    while (i < json.length) {
        val char = json[i]
        
        when (char) {
            '"' -> {
                // Обработка строк
                val start = i
                i++ // Пропускаем открывающую кавычку
                
                // Ищем закрывающую кавычку, учитывая экранированные символы
                while (i < json.length) {
                    if (json[i] == '"' && (i == 0 || json[i-1] != '\\')) {
                        i++ // Включаем закрывающую кавычку
                        break
                    }
                    i++
                }
                
                val stringValue = json.substring(start, i)
                
                // Определяем, является ли это ключом или значением
                val isKey = start > 0 && json.substring(0, start).trimEnd().endsWith('{') ||
                           json.substring(0, start).trimEnd().endsWith(',')
                
                withStyle(
                    style = SpanStyle(
                        color = if (isKey) Color(0xFF9C27B0) else Color(0xFF4CAF50), // Фиолетовый для ключей, зеленый для значений
                        fontWeight = if (isKey) FontWeight.Medium else FontWeight.Normal
                    )
                ) {
                    append(stringValue)
                }
            }
            
            '{', '}', '[', ']' -> {
                // Фигурные и квадратные скобки
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF2196F3), // Синий
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(char)
                }
                i++
            }
            
            ':', ',' -> {
                // Разделители
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF607D8B) // Серо-синий
                    )
                ) {
                    append(char)
                }
                i++
            }
            
            in '0'..'9', '-', '.' -> {
                // Числа
                val start = i
                while (i < json.length && (json[i].isDigit() || json[i] in "-.")) {
                    i++
                }
                
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFFFF5722) // Оранжево-красный
                    )
                ) {
                    append(json.substring(start, i))
                }
            }
            
            't', 'f', 'n' -> {
                // Логические значения и null
                val remaining = json.substring(i)
                when {
                    remaining.startsWith("true") -> {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF4CAF50), // Зеленый
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
                                color = Color(0xFFF44336), // Красный
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
                                color = Color(0xFF9E9E9E), // Серый
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
                // Остальные символы (пробелы, переносы строк и т.д.)
                append(char)
                i++
            }
        }
    }
}