package com.example.dlms_parser.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dlms_parser.domain.model.DlmsMessage
import com.example.dlms_parser.domain.model.ViewFormat
import org.jetbrains.compose.resources.stringResource
import dlms_parser.composeapp.generated.resources.Res
import dlms_parser.composeapp.generated.resources.*

@Composable
fun ResultsSection(
    messages: List<DlmsMessage>,
    selectedFormat: ViewFormat,
    onFormatChange: (ViewFormat) -> Unit,
    onClearResults: () -> Unit,
    onExportJson: () -> Unit,
    onCopyAsJson: () -> Unit,
    onCopyText: (String) -> Unit,
    onCopyMessageAsJson: (DlmsMessage) -> Unit,
    onExportMessageAsJson: (DlmsMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.results_title, messages.size),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(onClick = onExportJson) {
                            Text(
                                when (selectedFormat) {
                                    ViewFormat.JSON -> stringResource(Res.string.export_all_json)
                                    ViewFormat.XML -> "Export All XML"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        FilledTonalButton(onClick = onCopyAsJson) {
                            Text(
                                when (selectedFormat) {
                                    ViewFormat.JSON -> stringResource(Res.string.copy_all_json)
                                    ViewFormat.XML -> "Copy All XML"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        OutlinedButton(onClick = onClearResults) {
                            Text(
                                stringResource(Res.string.clear_results),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Global format toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Format:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Card(
                        modifier = Modifier.size(width = 120.dp, height = 36.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ViewFormat.entries.forEach { format ->
                                val isSelected = format == selectedFormat
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.Transparent
                                        }
                                    ),
                                    onClick = { onFormatChange(format) }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            format.getDisplayName(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(messages) { message ->
                    MessageCard(
                        message = message,
                        globalFormat = selectedFormat,
                        onCopyText = onCopyText,
                        onCopyAsJson = onCopyMessageAsJson,
                        onExportAsJson = onExportMessageAsJson
                    )
                }
            }
        }
    }
}