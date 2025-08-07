package com.example.dlms_parser.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dlms_parser.domain.model.*
import org.jetbrains.compose.resources.stringResource
import dlms_parser.composeapp.generated.resources.Res
import dlms_parser.composeapp.generated.resources.*

// Data classes для извлеченной информации из XML
data class ActionRequestXmlData(
    val invokeIdAndPriority: String? = null,
    val classId: String? = null,
    val instanceId: String? = null,
    val methodId: String? = null,
    val octetStringValue: String? = null,
    val uint32Value: String? = null
)

data class AareXmlData(
    val applicationContextName: String? = null,
    val associationResult: String? = null,
    val acseServiceUser: String? = null,
    val negotiatedDlmsVersionNumber: String? = null,
    val conformanceBits: List<String> = emptyList(),
    val negotiatedMaxPduSize: String? = null,
    val vaaName: String? = null
)

data class AarqXmlData(
    val applicationContextName: String? = null,
    val callingAPTitle: String? = null,
    val senderACSERequirements: String? = null,
    val mechanismName: String? = null,
    val callingAuthentication: String? = null,
    val proposedDlmsVersionNumber: String? = null,
    val proposedConformanceBits: List<String> = emptyList(),
    val proposedMaxPduSize: String? = null
)

data class GetResponseXmlData(
    val invokeIdAndPriority: String? = null,
    val octetStringValue: String? = null
)

// Функция для извлечения значений из XML без fallback'ов
private fun parseActionRequestXml(xml: String?): ActionRequestXmlData {
    if (xml == null) return ActionRequestXmlData()
    
    val data = ActionRequestXmlData(
        invokeIdAndPriority = extractXmlValue(xml, "InvokeIdAndPriority"),
        classId = extractXmlValue(xml, "ClassId"),
        instanceId = extractXmlValue(xml, "InstanceId"),
        methodId = extractXmlValue(xml, "MethodId"),
        octetStringValue = extractXmlValue(xml, "OctetString"),
        uint32Value = extractXmlValue(xml, "UInt32")
    )
    
    return data
}

// Простая функция извлечения значения атрибута Value из XML тега
private fun extractXmlValue(xml: String, tagName: String): String? {
    val pattern = """<$tagName[^>]*Value="([^"]*)"[^>]*>""".toRegex()
    return pattern.find(xml)?.groupValues?.get(1)
}

// Функция для парсинга AARE XML данных
private fun parseAareXml(xml: String?): AareXmlData {
    if (xml == null) return AareXmlData()
    
    // Извлекаем ConformanceBits
    val conformanceBits = mutableListOf<String>()
    val conformancePattern = """<ConformanceBit Name="([^"]*)"[^>]*>""".toRegex()
    conformancePattern.findAll(xml).forEach { match ->
        match.groupValues[1].let { conformanceBits.add(it) }
    }
    
    return AareXmlData(
        applicationContextName = extractXmlValue(xml, "ApplicationContextName"),
        associationResult = extractXmlValue(xml, "AssociationResult"),
        acseServiceUser = extractXmlValue(xml, "ACSEServiceUser"),
        negotiatedDlmsVersionNumber = extractXmlValue(xml, "NegotiatedDlmsVersionNumber"),
        conformanceBits = conformanceBits,
        negotiatedMaxPduSize = extractXmlValue(xml, "NegotiatedMaxPduSize"),
        vaaName = extractXmlValue(xml, "VaaName")
    )
}

// Функция для парсинга AARQ XML данных
private fun parseAarqXml(xml: String?): AarqXmlData {
    if (xml == null) return AarqXmlData()
    
    // Извлекаем Proposed ConformanceBits
    val proposedConformanceBits = mutableListOf<String>()
    val conformancePattern = """<ConformanceBit Name="([^"]*)"[^>]*>""".toRegex()
    conformancePattern.findAll(xml).forEach { match ->
        match.groupValues[1].let { proposedConformanceBits.add(it) }
    }
    
    return AarqXmlData(
        applicationContextName = extractXmlValue(xml, "ApplicationContextName"),
        callingAPTitle = extractXmlValue(xml, "CallingAPTitle"),
        senderACSERequirements = extractXmlValue(xml, "SenderACSERequirements"),
        mechanismName = extractXmlValue(xml, "MechanismName"),
        callingAuthentication = extractXmlValue(xml, "CallingAuthentication"),
        proposedDlmsVersionNumber = extractXmlValue(xml, "ProposedDlmsVersionNumber"),
        proposedConformanceBits = proposedConformanceBits,
        proposedMaxPduSize = extractXmlValue(xml, "ProposedMaxPduSize")
    )
}

// Функция для парсинга GetResponse XML данных
private fun parseGetResponseXml(xml: String?): GetResponseXmlData {
    if (xml == null) return GetResponseXmlData()
    
    return GetResponseXmlData(
        invokeIdAndPriority = extractXmlValue(xml, "InvokeIdAndPriority"),
        octetStringValue = extractXmlValue(xml, "OctetString")
    )
}

// Функция для декодирования hex строки в ASCII (без fallback)
private fun tryDecodeHexToAscii(hexString: String): String? {
    return try {
        val decoded = hexString.chunked(2).map { it.toInt(16).toChar() }.joinToString("")
        if (decoded.all { it.isLetterOrDigit() || it in "_" }) decoded else null
    } catch (e: Exception) {
        null
    }
}

// Маппинг для Application Context Name
private fun mapApplicationContextName(value: String): String {
    return when (value.uppercase()) {
        "LN" -> "Logical Name"
        "SN" -> "Short Name"
        else -> value
    }
}

// Маппинг для Association Result
private fun mapAssociationResult(value: String): String {
    return when (value) {
        "00" -> "Accepted"
        "01" -> "Rejected (permanent)"
        "02" -> "Rejected (transient)"
        else -> "Unknown"
    }
}

// Маппинг для ACSE Service User
private fun mapAcseServiceUser(value: String): String {
    return when (value) {
        "00" -> "No reason given"
        "01" -> "No common acse version"
        "02" -> "User data not readable"
        else -> "Unknown"
    }
}

// Маппинг для Sender ACSE Requirements
private fun mapSenderAcseRequirements(value: String): String {
    return when (value) {
        "0" -> "No authentication"
        "1" -> "Authentication required"
        else -> "Unknown"
    }
}

// Функция анализа OctetString - попытка извлечь различные типы данных
data class OctetStringAnalysis(
    val rawHex: String,
    val asciiDecoding: String? = null,
    val possibleTimestamp: String? = null,
    val possibleObisCode: String? = null,
    val possibleMeterSerial: String? = null,
    val structureInfo: String? = null,
    val hasReadableInterpretation: Boolean = false, // Есть ли уверенная интерпретация
    val primaryDisplay: String? = null // Основное отображение вместо raw данных
)

private fun analyzeOctetString(hexString: String): OctetStringAnalysis {
    if (hexString.length < 4) {
        return OctetStringAnalysis(rawHex = hexString)
    }
    
    val asciiDecoding: String?
    val possibleTimestamp: String?
    val possibleObisCode: String?
    val structureInfo: String?
    var hasReadableInterpretation = false
    var primaryDisplay: String? = null
    
    // Попытка ASCII декодирования (более строгая проверка)
    asciiDecoding = try {
        val decoded = hexString.chunked(2).map { it.toInt(16).toChar() }.joinToString("")
        if (decoded.length >= 3 && decoded.all { it.isLetterOrDigit() || it in " _-." }) {
            hasReadableInterpretation = true
            primaryDisplay = "Text: $decoded"
            decoded
        } else null
    } catch (_: Exception) {
        null
    }
    
    // Проверка на возможную структуру даты/времени DLMS (более уверенная)
    possibleTimestamp = if (hexString.length >= 24 && hexString.startsWith("07")) { // 12 bytes = 24 hex chars
        try {
            val bytes = hexString.chunked(2).map { it.toInt(16) }
            if (bytes.size >= 12) {
                val year = (bytes[0] shl 8) or bytes[1]
                val month = bytes[2]
                val day = bytes[3]
                val hour = bytes[5] // bytes[4] обычно dayOfWeek
                val minute = bytes[6]
                val second = bytes[7]
                
                if (year in 2000..2100 && month in 1..12 && day in 1..31 && 
                    hour in 0..23 && minute in 0..59 && second in 0..59) {
                    val timestamp = String.format("%04d-%02d-%02d %02d:%02d:%02d", 
                                                   year, month, day, hour, minute, second)
                    hasReadableInterpretation = true
                    primaryDisplay = "Timestamp: $timestamp"
                    timestamp
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    } else null
    
    // Проверка на OBIS код (более строгая - проверяем разумные значения)
    possibleObisCode = if (hexString.length >= 12) { // 6 bytes
        try {
            val bytes = hexString.chunked(2).map { it.toInt(16) }
            if (bytes.size >= 6) {
                // OBIS коды обычно имеют ограниченные диапазоны значений
                val obisCode = "${bytes[0]}.${bytes[1]}.${bytes[2]}.${bytes[3]}.${bytes[4]}.${bytes[5]}"
                if (bytes[0] in 0..255 && bytes[1] in 0..255 && bytes[5] == 255) { // Часто заканчиваются на FF
                    if (!hasReadableInterpretation) {
                        hasReadableInterpretation = true
                        primaryDisplay = "OBIS Code: $obisCode"
                    }
                    obisCode
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    } else null
    
    // Анализ структуры данных по первому байту (более детальный)
    structureInfo = try {
        val firstByte = hexString.substring(0, 2).toInt(16)
        when (firstByte) {
            0x09 -> {
                if (!hasReadableInterpretation) {
                    val length = if (hexString.length >= 4) (hexString.length - 2) / 2 else 0
                    primaryDisplay = "OctetString ($length bytes of binary data)"
                    hasReadableInterpretation = true
                }
                "OctetString type"
            }
            0x0A -> {
                // VisibleString - пытаемся декодировать содержимое
                if (hexString.length > 4) {
                    val content = hexString.substring(4) // Пропускаем тип и длину
                    val decoded = tryDecodeHexToAscii(content)
                    if (decoded != null) {
                        primaryDisplay = "Text: $decoded"
                        hasReadableInterpretation = true
                    }
                }
                "VisibleString type"
            }
            0x0C -> {
                // UTF8String
                if (hexString.length > 4) {
                    val content = hexString.substring(4)
                    val decoded = tryDecodeHexToAscii(content)
                    if (decoded != null) {
                        primaryDisplay = "Text: $decoded"
                        hasReadableInterpretation = true
                    }
                }
                "UTF8String type"
            }
            0x0F -> "Integer type"
            0x10 -> "Long type"
            0x12 -> "Unsigned type"
            0x16 -> "Enum type"
            0x01 -> {
                primaryDisplay = "Array of data elements"
                hasReadableInterpretation = true
                "Array type"
            }
            0x02 -> {
                primaryDisplay = "Structured data container"
                hasReadableInterpretation = true
                "Structure type"
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
    
    return OctetStringAnalysis(
        rawHex = hexString,
        asciiDecoding = asciiDecoding,
        possibleTimestamp = possibleTimestamp,
        possibleObisCode = possibleObisCode,
        possibleMeterSerial = null, // Не используется пока
        structureInfo = structureInfo,
        hasReadableInterpretation = hasReadableInterpretation,
        primaryDisplay = primaryDisplay
    )
}

@Composable
fun MessageCard(
    message: DlmsMessage,
    globalFormat: ViewFormat,
    onCopyText: (String) -> Unit,
    onCopyAsJson: (DlmsMessage) -> Unit,
    onExportAsJson: (DlmsMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        getMessageIcon(message.type),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        message.type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Export
                    OutlinedButton(
                        onClick = { onExportAsJson(message) },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when (globalFormat) {
                                ViewFormat.JSON -> stringResource(Res.string.export_json)
                                ViewFormat.XML -> "Export XML"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Copy
                    OutlinedButton(
                        onClick = { onCopyAsJson(message) },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when (globalFormat) {
                                ViewFormat.JSON -> stringResource(Res.string.copy_json)
                                ViewFormat.XML -> "Copy XML"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Copy Raw
                    OutlinedButton(
                        onClick = { onCopyText(message.rawData) },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            stringResource(Res.string.copy_raw),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            when (message) {
                is AarqMessage -> AarqDetails(message, globalFormat)
                is AareMessage -> AareDetails(message, globalFormat)
                is GetRequestMessage -> GetRequestDetails(message, globalFormat)
                is GetResponseMessage -> GetResponseDetails(message, globalFormat)
                is ActionRequestMessage -> ActionRequestDetails(message, globalFormat)
                is ActionResponseMessage -> ActionResponseDetails(message, globalFormat)
                is SetRequestMessage -> GenericMessageDetails(message, globalFormat)
                is SetResponseMessage -> GenericMessageDetails(message, globalFormat)
                is ReadRequestMessage -> GenericMessageDetails(message, globalFormat)
                is ReadResponseMessage -> GenericMessageDetails(message, globalFormat)
                is WriteRequestMessage -> GenericMessageDetails(message, globalFormat)
                is WriteResponseMessage -> GenericMessageDetails(message, globalFormat)
            }
        }
    }
}

@Composable
private fun AarqDetails(message: AarqMessage, globalFormat: ViewFormat) {
    val xmlData = parseAarqXml(message.originalXml)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Основная информация
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Application Context Name из XML или fallback
            xmlData.applicationContextName?.let { value ->
                DetailRow("Application Context Name", mapApplicationContextName(value))
            } ?: run {
                DetailRow(stringResource(Res.string.detail_application_context), message.applicationContextName)
            }
            
            // Calling AP Title
            xmlData.callingAPTitle?.let { value ->
                if (value.isNotEmpty()) {
                    DetailRow("Calling AP Title", value)
                }
            }
            
            // Sender ACSE Requirements
            xmlData.senderACSERequirements?.let { value ->
                DetailRow("Sender ACSE Requirements", "${mapSenderAcseRequirements(value)} (0x$value)")
            }
            
            // Mechanism Name
            xmlData.mechanismName?.let { value ->
                DetailRow("Mechanism Name", value)
            } ?: run {
                DetailRow(stringResource(Res.string.detail_mechanism_name), message.mechanismName)
            }
            
            // Calling Authentication
            xmlData.callingAuthentication?.let { value ->
                // Попробуем декодировать как ASCII если возможно
                tryDecodeHexToAscii(value)?.let { decoded ->
                    DetailRow("Calling Authentication", "$decoded (0x$value)")
                } ?: run {
                    DetailRow("Calling Authentication", value)
                }
            }
            
            // Initiate Request Details
            Text(
                text = "Initiate Request:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            // Proposed DLMS Version
            xmlData.proposedDlmsVersionNumber?.let { value ->
                val versionNum = value.toIntOrNull(16)
                DetailRow("Proposed DLMS Version", "${versionNum ?: value} (0x$value)")
            } ?: run {
                DetailRow(stringResource(Res.string.detail_dlms_version), message.initiateRequest.proposedDlmsVersionNumber.toString())
            }
            
            // Proposed Conformance
            if (xmlData.proposedConformanceBits.isNotEmpty()) {
                Text(
                    text = "Proposed Conformance:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                xmlData.proposedConformanceBits.forEach { conformanceBit ->
                    Text(
                        text = "  • $conformanceBit",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                DetailRow(stringResource(Res.string.detail_conformance), message.initiateRequest.proposedConformance.joinToString(", "))
            }
            
            // Proposed Max PDU Size
            xmlData.proposedMaxPduSize?.let { value ->
                val sizeValue = value.toIntOrNull(16)
                DetailRow("Proposed Max PDU Size", "${sizeValue ?: value} bytes (0x$value)")
            } ?: run {
                DetailRow(stringResource(Res.string.detail_max_pdu_size), message.initiateRequest.clientMaxReceivePduSize.toString())
            }
        }
        
        // JSON/XML структура
        message.xmlStructure?.let { xmlStructure ->
            StructureViewer(
                json = xmlStructure,
                originalXml = message.originalXml,
                globalFormat = globalFormat
            )
        }
    }
}

@Composable
private fun AareDetails(message: AareMessage, globalFormat: ViewFormat) {
    val xmlData = parseAareXml(message.originalXml)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Основная информация
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Application Context Name из XML или fallback
            xmlData.applicationContextName?.let { value ->
                DetailRow("Application Context Name", mapApplicationContextName(value))
            } ?: run {
                DetailRow(stringResource(Res.string.detail_application_context), message.applicationContextName)
            }
            
            // Association Result
            xmlData.associationResult?.let { value ->
                DetailRow("Association Result", "${mapAssociationResult(value)} (0x$value)")
            } ?: run {
                DetailRow(stringResource(Res.string.detail_association_result), message.associationResult.name)
            }
            
            // Result Source Diagnostic
            xmlData.acseServiceUser?.let { value ->
                DetailRow("ACSE Service User", "${mapAcseServiceUser(value)} (0x$value)")
            }
            
            // Initiate Response Details
            Text(
                text = "Initiate Response:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            // Negotiated DLMS Version
            xmlData.negotiatedDlmsVersionNumber?.let { value ->
                val versionNum = value.toIntOrNull(16)
                DetailRow("Negotiated DLMS Version", "${versionNum ?: value} (0x$value)")
            } ?: run {
                DetailRow(stringResource(Res.string.detail_dlms_version), message.initiateResponse.negotiatedDlmsVersionNumber.toString())
            }
            
            // Negotiated Conformance
            if (xmlData.conformanceBits.isNotEmpty()) {
                Text(
                    text = "Negotiated Conformance:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                xmlData.conformanceBits.forEach { conformanceBit ->
                    Text(
                        text = "  • $conformanceBit",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            // Negotiated Max PDU Size
            xmlData.negotiatedMaxPduSize?.let { value ->
                val sizeValue = value.toIntOrNull(16)
                DetailRow("Negotiated Max PDU Size", "${sizeValue ?: value} bytes (0x$value)")
            } ?: run {
                DetailRow(stringResource(Res.string.detail_max_pdu_size), message.initiateResponse.serverMaxReceivePduSize.toString())
            }
            
            // VAA Name
            xmlData.vaaName?.let { value ->
                val vaaNum = value.toIntOrNull(16)
                DetailRow("VAA Name", "${vaaNum ?: value} (0x$value)")
            } ?: run {
                DetailRow(stringResource(Res.string.detail_vaa_name), message.initiateResponse.vaaName.toString())
            }
        }
        
        // JSON/XML структура
        message.xmlStructure?.let { xmlStructure ->
            StructureViewer(
                json = xmlStructure,
                originalXml = message.originalXml,
                globalFormat = globalFormat
            )
        }
    }
}

@Composable
private fun GetRequestDetails(message: GetRequestMessage, globalFormat: ViewFormat) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Основная информация
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DetailRow(stringResource(Res.string.detail_invoke_id), "0x${message.invokeId.toString(16).uppercase()}")
            DetailRow(stringResource(Res.string.detail_attribute), message.attribute)
            DetailRow(stringResource(Res.string.detail_request_type), message.requestType.name)
        }
        
        // JSON структура
        message.xmlStructure?.let { xmlStructure ->
            StructureViewer(
                json = xmlStructure,
                originalXml = message.originalXml,
                globalFormat = globalFormat
            )
        }
    }
}

@Composable
private fun GetResponseDetails(message: GetResponseMessage, globalFormat: ViewFormat) {
    val xmlData = parseGetResponseXml(message.originalXml)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Основная информация
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Invoke ID из XML или fallback
            xmlData.invokeIdAndPriority?.let { value ->
                val invokeId = value.toIntOrNull(16)
                DetailRow("Invoke ID & Priority", "${invokeId ?: value} (0x$value)")
            } ?: run {
                DetailRow(stringResource(Res.string.detail_invoke_id), "${message.invokeId} (0x${message.invokeId.toString(16).uppercase()})")
            }
            
            DetailRow(stringResource(Res.string.detail_data_type), message.dataType)
            DetailRow(stringResource(Res.string.detail_response_type), message.responseType.name)
            
            // Анализ OctetString из XML
            xmlData.octetStringValue?.let { octetString ->
                val analysis = analyzeOctetString(octetString)
                
                Text(
                    text = "Response Data:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                // Показываем primaryDisplay если есть читаемая интерпретация, иначе raw данные
                if (analysis.hasReadableInterpretation && analysis.primaryDisplay != null) {
                    DetailRow("Data Content", analysis.primaryDisplay)
                    
                    // Показываем дополнительные детали, если есть
                    analysis.structureInfo?.let { info ->
                        DetailRow("Data Type", info)
                    }
                    
                    // Показываем длину только если это полезно
                    val length = analysis.rawHex.length / 2
                    DetailRow("Data Length", "$length bytes")
                    
                    // Raw данные в отдельной секции (свернуто по умолчанию)
                    Text(
                        text = "Technical Details:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    SelectionContainer {
                        Text(
                            text = "Raw Hex: ${analysis.rawHex}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    // Fallback к анализу если нет уверенной интерпретации
                    SelectionContainer {
                        Text(
                            text = "Raw Data: ${analysis.rawHex}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Показываем возможные интерпретации
                    analysis.asciiDecoding?.let { ascii ->
                        DetailRow("Possible ASCII", ascii)
                    }
                    
                    analysis.possibleTimestamp?.let { timestamp ->
                        DetailRow("Possible Timestamp", timestamp)
                    }
                    
                    analysis.possibleObisCode?.let { obis ->
                        DetailRow("Possible OBIS Code", obis)
                    }
                    
                    analysis.structureInfo?.let { info ->
                        DetailRow("Data Type", info)
                    }
                    
                    val length = analysis.rawHex.length / 2
                    DetailRow("Data Length", "$length bytes")
                }
                
            } ?: run {
                // Fallback к старому отображению
                SelectionContainer {
                    Text(
                        stringResource(Res.string.detail_data) + ": ${message.data}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // JSON/XML структура
        message.xmlStructure?.let { xmlStructure ->
            StructureViewer(
                json = xmlStructure,
                originalXml = message.originalXml,
                globalFormat = globalFormat
            )
        }
    }
}

@Composable
private fun ActionRequestDetails(message: ActionRequestMessage, globalFormat: ViewFormat) {
    val xmlData = parseActionRequestXml(message.originalXml)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Основная информация
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Показываем данные из XML, если доступны, иначе из модели
            xmlData.invokeIdAndPriority?.let { value ->
                val invokeId = value.toIntOrNull(16)
                DetailRow("Invoke ID & Priority", "${invokeId ?: value} (0x$value)")
            } ?: run {
                DetailRow(stringResource(Res.string.detail_invoke_id), "0x${message.invokeId.toString(16).uppercase()}")
            }
            
            DetailRow(stringResource(Res.string.detail_method), message.method)
            DetailRow(stringResource(Res.string.detail_request_type), message.requestType.name)
            
            // Детали из XML - Method Descriptor
            xmlData.classId?.let { value ->
                val classIdNum = value.toIntOrNull(16)
                DetailRow("Class ID", "${classIdNum ?: value} (0x$value)")
            }
            xmlData.instanceId?.let { value ->
                DetailRow("Instance ID", value) // Instance ID лучше оставить как есть, так как это OBIS код
            }
            xmlData.methodId?.let { value ->
                val methodIdNum = value.toIntOrNull(16)
                DetailRow("Method ID", "${methodIdNum ?: value} (0x$value)")
            }
            
            // Method Invocation Parameters
            if (xmlData.octetStringValue != null || xmlData.uint32Value != null) {
                Text(
                    text = "Method Invocation Parameters:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            xmlData.octetStringValue?.let { value ->
                DetailRow("OctetString", value)
                // Попробуем декодировать как ASCII если возможно
                tryDecodeHexToAscii(value)?.let { decoded ->
                    DetailRow("OctetString (ASCII)", decoded)
                }
            }
            
            xmlData.uint32Value?.let { value ->
                val longValue = value.toLongOrNull(16)
                DetailRow("UInt32", "${longValue ?: value} (0x$value)")
            }
            
            if (xmlData.invokeIdAndPriority == null && xmlData.classId == null) {
                // Fallback к базовой информации только если XML данных нет
                DetailRow(stringResource(Res.string.detail_parameters), stringResource(Res.string.detail_parameters_count, message.parameters.structure.size))
            }
        }
        
        // JSON/XML структура
        message.xmlStructure?.let { xmlStructure ->
            StructureViewer(
                json = xmlStructure,
                originalXml = message.originalXml,
                globalFormat = globalFormat
            )
        }
    }
}

@Composable
private fun ActionResponseDetails(message: ActionResponseMessage, globalFormat: ViewFormat) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Основная информация
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DetailRow(stringResource(Res.string.detail_invoke_id), "0x${message.invokeId.toString(16).uppercase()}")
            DetailRow(stringResource(Res.string.detail_action_result), message.actionResult.name)
            DetailRow(stringResource(Res.string.detail_response_type), message.responseType.name)
        }
        
        // JSON структура
        message.xmlStructure?.let { xmlStructure ->
            StructureViewer(
                json = xmlStructure,
                originalXml = message.originalXml,
                globalFormat = globalFormat
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        SelectionContainer {
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun GenericMessageDetails(message: DlmsMessage, globalFormat: ViewFormat) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Основная информация
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Generic DLMS message: ${message.type}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Raw data length: ${message.rawData.length} characters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // JSON структура
        message.xmlStructure?.let { xmlStructure ->
            StructureViewer(
                json = xmlStructure,
                originalXml = message.originalXml,
                globalFormat = globalFormat
            )
        }
    }
}

private fun getMessageIcon(type: String): String {
    return when (type) {
        "AARQ" -> "🔐"
        "AARE" -> "🔓"
        "GetRequest" -> "📥"
        "GetResponse" -> "📤"
        "ActionRequest" -> "⚡"
        "ActionResponse" -> "🔄"
        "SetRequest" -> "📝"
        "SetResponse" -> "✅"
        "ReadRequest" -> "👀"
        "ReadResponse" -> "📊"
        "WriteRequest" -> "✏️"
        "WriteResponse" -> "💾"
        else -> "📄"
    }
}