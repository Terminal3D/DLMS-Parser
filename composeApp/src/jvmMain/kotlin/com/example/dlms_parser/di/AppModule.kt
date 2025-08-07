package com.example.dlms_parser.di

import com.example.dlms_parser.data.parser.GuruxDlmsParser
import com.example.dlms_parser.data.repository.DlmsParserRepositoryImpl
import com.example.dlms_parser.data.repository.ExportRepositoryImpl
import com.example.dlms_parser.data.repository.PreferencesRepository
import com.example.dlms_parser.data.datastore.dataStore
import com.example.dlms_parser.domain.repository.DlmsParserRepository
import com.example.dlms_parser.domain.repository.ExportRepository
import com.example.dlms_parser.domain.repository.HistoryRepository
import com.example.dlms_parser.domain.repository.LocaleRepository
import com.example.dlms_parser.util.LocaleRepositoryImpl
import com.example.dlms_parser.domain.usecase.ExportMessagesUseCase
import com.example.dlms_parser.domain.usecase.HistoryUseCase
import com.example.dlms_parser.domain.usecase.ParseDlmsDataUseCase
import com.example.dlms_parser.domain.usecase.ParseMultipleDlmsDataUseCase
import com.example.dlms_parser.domain.usecase.ValidateHexDataUseCase
import com.example.dlms_parser.presentation.DlmsParserViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::GuruxDlmsParser)
    singleOf(::DlmsParserRepositoryImpl) bind DlmsParserRepository::class
    single { PreferencesRepository(dataStore) } bind HistoryRepository::class
    single { com.example.dlms_parser.data.repository.ThemePreferencesRepository(dataStore) }
    singleOf(::ExportRepositoryImpl) bind ExportRepository::class
    singleOf(::LocaleRepositoryImpl) bind LocaleRepository::class
}

val domainModule = module {
    singleOf(::ParseDlmsDataUseCase)
    singleOf(::ParseMultipleDlmsDataUseCase)
    singleOf(::ValidateHexDataUseCase)
    singleOf(::HistoryUseCase)
    singleOf(::ExportMessagesUseCase)
}

val presentationModule = module {
    factory { 
        DlmsParserViewModel(
            parseDlmsDataUseCase = get(),
            parseMultipleDlmsDataUseCase = get(),
            validateHexDataUseCase = get(),
            historyUseCase = get(),
            exportMessagesUseCase = get(),
            themePreferencesRepository = get<com.example.dlms_parser.data.repository.ThemePreferencesRepository>(),
            localeRepository = get()
        )
    }
}

val appModules = listOf(
    dataModule,
    domainModule,
    presentationModule
)