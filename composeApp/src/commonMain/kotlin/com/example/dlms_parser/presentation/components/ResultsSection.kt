package com.example.dlms_parser.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onExportJson,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when (selectedFormat) {
                                ViewFormat.JSON -> stringResource(Res.string.export_all_json)
                                ViewFormat.XML -> stringResource(Res.string.export_all_xml)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    FilledTonalButton(
                        onClick = onCopyAsJson,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when (selectedFormat) {
                                ViewFormat.JSON -> stringResource(Res.string.copy_all_json)
                                ViewFormat.XML -> stringResource(Res.string.copy_all_xml)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onClearResults,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            stringResource(Res.string.clear_results),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Format:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    AnimatedFormatToggle(
                        selectedFormat = selectedFormat,
                        onFormatChange = onFormatChange,
                        modifier = Modifier.size(width = 120.dp, height = 36.dp)
                    )
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

@Composable
private fun AnimatedFormatToggle(
    selectedFormat: ViewFormat,
    onFormatChange: (ViewFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = ViewFormat.entries.indexOf(selectedFormat)
    val animatedOffset by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val selectedColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val selectedTextColor = MaterialTheme.colorScheme.onPrimary
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val segmentWidth = size.width / ViewFormat.entries.size
                    val indicatorLeft = animatedOffset * segmentWidth
                    val indicatorWidth = segmentWidth
                    
                    drawRoundRect(
                        color = selectedColor,
                        topLeft = androidx.compose.ui.geometry.Offset(indicatorLeft + 2.dp.toPx(), 2.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(indicatorWidth - 4.dp.toPx(), size.height - 4.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                    )
                }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ViewFormat.entries.forEachIndexed { index, format ->
                    val isSelected = format == selectedFormat
                    val animatedTextColor by animateColorAsState(
                        targetValue = if (isSelected) selectedTextColor else textColor,
                        animationSpec = tween<Color>(durationMillis = 200)
                    )
                    val animatedFontWeight by animateFloatAsState(
                        targetValue = if (isSelected) 700f else 400f,
                        animationSpec = tween<Float>(durationMillis = 200)
                    )
                    
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        onClick = { onFormatChange(format) }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = format.getDisplayName(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight(animatedFontWeight.toInt())
                                ),
                                color = animatedTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}