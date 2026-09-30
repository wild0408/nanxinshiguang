import com.android.build.api.variant.FilterConfiguration

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.androidx.room3)
    alias(libs.plugins.wire)
    alias(libs.plugins.aboutLibraries)
}

val releaseStoreFile = providers.environmentVariable("NANXINSHIGUANG_KEYSTORE_FILE").orNull
val releaseStorePassword = providers.environmentVariable("NANXINSHIGUANG_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("NANXINSHIGUANG_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("NANXINSHIGUANG_KEY_PASSWORD").orNull
val hasReleaseSigning = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

kotlin {
    androidTarget()
    jvmToolchain(21)

    sourceSets {
        commonMain {
            kotlin.srcDir("src/commonMain/kotlin")
            kotlin.srcDir(layout.buildDirectory.dir("generated/source/wire/shared"))
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.material3.adaptive)
                implementation(libs.compose.material3.adaptive.navigation.suite)
                implementation(libs.compose.ui)
                implementation(libs.compose.animation)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.miuix.ui)
                implementation(libs.miuix.icons)
                implementation(libs.miuix.preference)
                implementation(libs.material.kolor)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.androidx.navigation3.ui)
                implementation(libs.androidx.lifecycle.viewmodel.navigation3)
                implementation(libs.coil.compose)
                implementation(libs.aboutlibraries.compose.m3)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose.navigation3)
                implementation(libs.koin.annotations)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.cbor)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kgit)
                implementation(libs.okio)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.auth)
                implementation(libs.androidx.room3.runtime)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.datastore.core)
                implementation(libs.wire.runtime)
            }
        }
        androidMain {
            kotlin.srcDir("src/androidMain/kotlin")
            dependencies {
                implementation(libs.androidx.sqlite.framework)
                implementation(libs.ktor.client.cio)
            }
        }
        commonTest {
            kotlin.srcDir("src/commonTest/kotlin")
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "com.wild0408.nanxinshiguang"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/main/AndroidManifest.xml")

    signingConfigs {
        if (hasReleaseSigning) {
            create("nanxinshiguangRelease") {
                storeFile = file(requireNotNull(releaseStoreFile))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.wild0408.nanxinshiguang"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("nanxinshiguangRelease")
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    splits {
        abi {
            isEnable = true
            exclude("mips", "mips64", "armeabi", "riscv64", "x86")
            isUniversalApk = false
            include("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = true
        localeFilters += listOf("zh", "zh-rCN", "zh-rTW", "en")
    }
}

// Only one Wire generation task writes the common source directory for both Android variants.
tasks.matching {
    it.name == "compileReleaseKotlinAndroid" || it.name == "kspReleaseKotlinAndroid"
}.configureEach {
    dependsOn(tasks.named("generateDebugProtos"))
}

tasks.matching { it.name == "generateReleaseProtos" }.configureEach {
    enabled = false
}

compose.resources {
    packageOfResClass = "nanxinshiguang.generated.resources"
    customDirectory("commonMain", layout.projectDirectory.dir("src/commonMain/composeResources").let { provider { it } })
}

dependencies {
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.adaptive)
    implementation(libs.compose.material3.adaptive.navigation.suite)
    implementation(libs.compose.ui)
    implementation(libs.compose.animation)
    implementation(libs.compose.components.resources)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.miuix.ui)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.preference)
    implementation(libs.material.kolor)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.coil.compose)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose.navigation3)
    implementation(libs.koin.annotations)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.cbor)
    implementation(libs.kotlinx.datetime)
    implementation(libs.okio)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.auth)
    implementation(libs.androidx.room3.runtime)
    implementation(libs.androidx.sqlite.framework)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)
    implementation(libs.wire.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.kotlin.test)
    debugImplementation(libs.compose.ui.tooling)
    add("kspAndroid", libs.androidx.room3.compiler)
}

aboutLibraries {
    export {
        outputPath = file("src/commonMain/composeResources/files/aboutlibraries.json")
        prettyPrint = true
    }
    library {
        duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

wire {
    sourcePath {
        srcDir("src/commonMain/proto")
    }
    kotlin {
        out = layout.buildDirectory.dir("generated/source/wire/shared").get().asFile.absolutePath
        escapeKotlinKeywords = true
        enumMode = "enum_class"
        rpcRole = "none"
    }
}

val exportLibraryDefinitions = tasks.named("exportLibraryDefinitions")

tasks.matching {
    it.name.startsWith("generateComposeResClass") ||
            it.name.startsWith("copyNonXmlValueResources") ||
            it.name.startsWith("prepareComposeResources")
}.configureEach {
    dependsOn(exportLibraryDefinitions)
}

androidComponents {
    onVariants { variant ->
        val buildType = variant.buildType ?: ""
        val versionName = android.defaultConfig.versionName ?: ""
        variant.outputs.forEach { output ->
            val abiFilter = output.filters.find {
                it.filterType == FilterConfiguration.FilterType.ABI
            }?.identifier ?: "universal"
            output.outputFileName.set("nanxinshiguang-v${versionName}-${abiFilter}-${buildType}.apk")
        }
    }
}
