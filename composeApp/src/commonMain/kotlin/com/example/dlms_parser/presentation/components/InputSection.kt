package com.example.dlms_parser.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dlms_parser.presentation.DlmsParserState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import dlms_parser.composeapp.generated.resources.Res
import dlms_parser.composeapp.generated.resources.*

@Composable
fun InputSection(
    state: DlmsParserState,
    onInputChange: (String) -> Unit,
    onParseMultiple: () -> Unit,
    onLoadExamples: () -> Unit,
    onClearInput: () -> Unit,
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.input_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (state.inputText.isNotBlank()) {
                    Spacer(Modifier.width(12.dp))
                    OutlinedIconButton(
                        onClick = onClearInput,
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Clear,
                            contentDescription = stringResource(Res.string.clear_input),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = state.inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { 
                    Text(
                        stringResource(Res.string.input_placeholder),
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                isError = !state.isInputValid,
                supportingText = if (!state.isInputValid) {
                    {
                        Text(
                            stringResource(Res.string.input_validation_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    {
                        val lineCount = state.inputText.lines().filter { it.trim().isNotBlank() }.size
                        Text(
                            stringResource(Res.string.input_line_count, lineCount),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onParseMultiple,
                    enabled = !state.isLoading && state.isInputValid && state.inputText.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        stringResource(Res.string.parse_multiple),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                OutlinedButton(
                    onClick = onLoadExamples,
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(Res.string.load_examples),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}