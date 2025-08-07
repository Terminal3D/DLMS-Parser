package com.example.dlms_parser.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import okio.Path.Companion.toPath

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"

fun createDlmsParserDataStore(): DataStore<Preferences> {
    val os = System.getProperty("os.name").lowercase()
    val userHome = System.getProperty("user.home")
    val appDataDir = when {
        os.contains("win") -> File(System.getenv("APPDATA"), "DlmsParser")
        os.contains("mac") -> File(userHome, "Library/Application Support/DlmsParser")
        else -> File(userHome, ".local/share/DlmsParser")
    }

    if(!appDataDir.exists()) {
        appDataDir.mkdirs()
    }

    val prefsFile = File(appDataDir, DATA_STORE_FILE_NAME)

    return createDataStore { prefsFile.absolutePath }
}

val dataStore: DataStore<Preferences> = createDlmsParserDataStore()