2# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **DLMS parser application** built with Kotlin and Compose Multiplatform, targeting desktop platforms. DLMS (Device Language Message Specification) is a protocol used in smart meters and energy management systems. The application parses encrypted DLMS/COSEM data and displays it in human-readable format.

## Build System & Dependencies

- **Build System**: Gradle with Kotlin DSL
- **Primary Language**: Kotlin 2.2.0
- **UI Framework**: Compose Multiplatform 1.8.2
- **DLMS Library**: Gurux.DLMS 4.0.79
- **Dependency Injection**: Koin 4.1.0
- **Serialization**: kotlinx.serialization 1.7.3
- **Target Platform**: JVM/Desktop

## Common Development Commands

```bash
# Run the desktop application
./gradlew :composeApp:run

# Run tests
./gradlew test

# Build the project
./gradlew build

# Create distributable packages (DMG, MSI, DEB)
./gradlew :composeApp:package

# Create application bundle
./gradlew :composeApp:createDistributable
```

## Architecture & Code Organization

The project follows **Clean Architecture** principles with clear separation of concerns:

### Common Main (`src/commonMain/kotlin/`)
- **Domain Layer** (`domain/`)
  - `model/` - Data models (DlmsMessage, ParseResult, etc.)
  - `repository/` - Repository interfaces
  - `usecase/` - Business logic use cases
- **Presentation Layer** (`presentation/`)
  - `DlmsParserViewModel.kt` - Main ViewModel with state management
- **App Layer**
  - `App.kt` - Main Compose UI

### JVM Main (`src/jvmMain/kotlin/`)
- **Data Layer** (`data/`)
  - `parser/GuruxDlmsParser.kt` - Gurux.DLMS integration
  - `repository/DlmsParserRepositoryImpl.kt` - Repository implementation
- **DI Layer** (`di/`)
  - `AppModule.kt` - Koin dependency injection modules
- **Platform Layer**
  - `main.kt` - Application entry point with DI initialization

### Test Structure (`src/jvmTest/kotlin/`)
- **Data Tests** - Parser and repository tests
- **Domain Tests** - Use case tests with sample data

## DLMS Message Types Supported

The application parses the following DLMS message types:
- **AARQ** (Association Request) - Connection initiation
- **AARE** (Association Response) - Connection response
- **GetRequest** - Data request messages
- **GetResponse** - Data response messages
- **ActionRequest** - Method execution requests
- **ActionResponse** - Method execution responses

## Key Dependencies

```toml
# Core dependencies
gurux-dlms = "4.0.79"                    # DLMS parsing library
kotlinx-serialization = "1.7.3"         # Data serialization
koin = "4.1.0"                          # Dependency injection
jackson = "2.18.2"                      # XML/JSON processing

# UI dependencies
compose-multiplatform = "1.8.2"         # UI framework
material3 = "1.8.2"                     # Material 3 design

# Data persistence
datastore = "1.1.1"                     # Preferences storage

# Testing dependencies
kotlinx-coroutines-test = "1.10.2"      # Coroutine testing
```

## Sample Data

The application includes test cases with real DLMS data examples for:
- AARQ: `60 3A A1 09 06 07 60 85 74 05 08 01 01...`
- AARE: `61 29 A1 09 06 07 60 85 74 05 08 01 01...`
- GetRequest: `C0 01 4F 00 01 00 00 81 00 00 00 02 00`
- GetResponse: `C4 01 4F 00 09 1E 00 0C C3 13 02 07...`
- ActionRequest: `C3 01 42 00 12 00 00 2C 00 00 FF...`
- ActionResponse: `C7 01 42 0C 00`

## Key Features

- **Multi-format Support**: JSON/XML toggle for data visualization and export
- **Enhanced Message Analysis**: Detailed parsing of ActionRequest, AARE, AARQ, and GetResponse messages
- **Intelligent OctetString Analysis**: Automatic detection of timestamps, OBIS codes, ASCII text, and structured data
- **Smart Display Logic**: Human-readable values first, hex values in parentheses, hiding raw data when confident interpretation exists
- **Data Persistence**: History and theme preferences saved using DataStore
- **Responsive UI**: Adaptive button layout for different screen sizes
- **Cross-platform Packaging**: DMG (macOS), MSI (Windows), DEB (Linux), and universal JAR support

## UI Components

### Core Components
- **StructureViewer**: Unified JSON/XML viewer with syntax highlighting and collapsible sections
- **MessageComponents**: Enhanced displays for different DLMS message types with detailed field extraction
- **ResultsSection**: Adaptive layout with format toggle and export functionality

### Data Processing
- **XmlToJsonConverter**: Professional Jackson-based XML to JSON conversion with fallback handling
- **OctetStringAnalysis**: Multi-format data interpretation (timestamps, OBIS codes, ASCII, structures)

## Development Notes

- **Platform-specific code**: Gurux.DLMS integration is JVM-only in `jvmMain`
- **State management**: Uses Compose State and ViewModel pattern with Koin DI
- **Error handling**: ParseResult wrapper for safe error handling with detailed user feedback
- **UI validation**: Real-time hex format validation with visual feedback
- **Data persistence**: Cross-platform DataStore with fallback paths for Windows packaging
- **Testability**: Comprehensive unit test coverage with real DLMS sample data