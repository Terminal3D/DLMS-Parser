package com.example.dlms_parser.data.repository

import com.example.dlms_parser.domain.model.DlmsMessage
import com.example.dlms_parser.domain.model.ParseResult
import com.example.dlms_parser.domain.model.ViewFormat
import com.example.dlms_parser.domain.repository.ExportRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class ExportRepositoryImpl : ExportRepository {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
        classDiscriminator = "messageClass"
    }
    
    override suspend fun exportToJson(messages: List<DlmsMessage>): ParseResult<String> {
        return try {
            val jsonString = json.encodeToString(messages)
            ParseResult.Success(jsonString)
        } catch (e: Exception) {
            ParseResult.Error("Failed to export to JSON", e.message)
        }
    }
    
    override suspend fun exportToXml(messages: List<DlmsMessage>): ParseResult<String> {
        return try {
            val xmlBuilder = StringBuilder()
            xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            xmlBuilder.append("<DlmsMessages>\n")
            
            messages.forEach { message ->
                xmlBuilder.append("  <Message>\n")
                xmlBuilder.append("    <Type>${message.type}</Type>\n")
                xmlBuilder.append("    <RawData>${message.rawData}</RawData>\n")
                
                if (message.originalXml != null) {
                    xmlBuilder.append("    <Structure>\n")
                    xmlBuilder.append("      <![CDATA[${message.originalXml}]]>\n")
                    xmlBuilder.append("    </Structure>\n")
                } else if (message.xmlStructure != null) {
                    xmlBuilder.append("    <JsonStructure>\n")
                    xmlBuilder.append("      <![CDATA[${message.xmlStructure}]]>\n")
                    xmlBuilder.append("    </JsonStructure>\n")
                }
                
                xmlBuilder.append("  </Message>\n")
            }
            
            xmlBuilder.append("</DlmsMessages>")
            ParseResult.Success(xmlBuilder.toString())
        } catch (e: Exception) {
            ParseResult.Error("Failed to export to XML", e.message)
        }
    }
    
    override suspend fun exportToFormat(messages: List<DlmsMessage>, format: ViewFormat): ParseResult<String> {
        return when (format) {
            ViewFormat.JSON -> exportToJson(messages)
            ViewFormat.XML -> exportToXml(messages)
        }
    }
    
    override suspend fun copyToClipboard(text: String): ParseResult<Unit> {
        return try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val selection = StringSelection(text)
            clipboard.setContents(selection, null)
            ParseResult.Success(Unit)
        } catch (e: Exception) {
            ParseResult.Error("Failed to copy to clipboard", e.message)
        }
    }
    
    override suspend fun saveToFile(content: String, fileName: String): ParseResult<String> {
        return try {
            val fileChooser = JFileChooser().apply {
                selectedFile = File(fileName)
                fileFilter = FileNameExtensionFilter("JSON files", "json")
            }
            
            val result = fileChooser.showSaveDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                file.writeText(content)
                ParseResult.Success(file.absolutePath)
            } else {
                ParseResult.Error("Save operation cancelled")
            }
        } catch (e: Exception) {
            ParseResult.Error("Failed to save file", e.message)
        }
    }
}