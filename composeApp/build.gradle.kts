import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.gurux.dlms)
            implementation(libs.datastore.preferences)
            implementation(libs.json)
            implementation(libs.jackson.dataformat.xml)
            implementation(libs.jackson.core)
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.example.dlms_parser.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.example.dlms_parser"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register<JavaExec>("testXmlOutput") {
    mainClass.set("com.example.dlms_parser.TestXmlOutputKt")
    classpath = sourceSets["jvmMain"].runtimeClasspath
}

tasks.register<JavaExec>("testJsonOutput") {
    mainClass.set("com.example.dlms_parser.TestJsonOutputKt")
    classpath = sourceSets["jvmMain"].runtimeClasspath
}

tasks.register<JavaExec>("testXmlCleaner") {
    mainClass.set("com.example.dlms_parser.TestXmlCleanerKt")
    classpath = sourceSets["jvmMain"].runtimeClasspath
}
