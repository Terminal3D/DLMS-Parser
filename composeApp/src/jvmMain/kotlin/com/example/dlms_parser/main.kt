package com.example.dlms_parser

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.dlms_parser.di.appModules
import org.koin.core.context.startKoin

fun main() = application {
    val koinApp = startKoin {
        modules(appModules)
    }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "DLMS Parser - Smart Meter Data Analyzer",
    ) {
        App()
    }
}