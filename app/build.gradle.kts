import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
//    alias(libs.plugins.jetbrains.kotlin.android)

    // Kotlin serialization plugin for type safe routes and navigation arguments
    kotlin("plugin.serialization") version "2.0.21"
    alias(libs.plugins.compose.compiler)

    id("com.google.devtools.ksp")

    id("kotlin-parcelize")

    id("dagger.hilt.android.plugin")

//    id("androidx.room")
//    id("kotlinx-serialization")
}

//// Creates a variable called keystorePropertiesFile, and initializes it to the
//// keystore.properties file.
//def keystorePropertiesFile = rootProject.file("keystore.properties")
//
//// Initializes a new Properties() object called keystoreProperties.
//def keystoreProperties = new Properties()
//
//// Loads the keystore.properties file into the keystoreProperties object.
//keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

android {
    namespace = "com.eternalfairy.lotus"
    compileSdk = 36

//    signingConfigs {
//        config {
//            POSTHOG_HOST_URL keystoreProperties['POSTHOG_HOST_URL']
//            POSTHOG_PROJECT_TOKEN keystoreProperties['POSTHOG_PROJECT_TOKEN']
//            POWERSYNC_URL file(keystoreProperties['POWERSYNC_URL'])
//            SUPABASE_KEY keystoreProperties['SUPABASE_KEY']
//            SUPABASE_URL keystoreProperties['SUPABASE_URL']
//        }
//    }

    defaultConfig {
        applicationId = "com.eternalfairy.lotus"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val keystoreProperties = Properties()
    val keystorePropertiesFile = File(rootDir, "keystore.properties")

    if (keystorePropertiesFile.exists() && keystorePropertiesFile.isFile) {
        keystorePropertiesFile.inputStream().use {
            keystoreProperties.load(it)
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                type = "String",
                name = "POSTHOG_HOST_URL",
                value = keystoreProperties.getProperty("POSTHOG_HOST_URL")
            )
            buildConfigField(
                type = "String",
                name = "POSTHOG_PROJECT_TOKEN",
                value = keystoreProperties.getProperty("POSTHOG_PROJECT_TOKEN")
            )
            buildConfigField(
                type = "String",
                name = "POWERSYNC_URL",
                value = keystoreProperties.getProperty("POWERSYNC_URL")
            )
            buildConfigField(
                type = "String",
                name = "SUPABASE_KEY",
                value = keystoreProperties.getProperty("SUPABASE_KEY")
            )
            buildConfigField(
                type = "String",
                name = "SUPABASE_URL",
                value = keystoreProperties.getProperty("SUPABASE_URL")
            )
        }
    }
    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
//    kotlin {
//        compilerOptions {
//            languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
//            // Optional: Set jvmTarget
//            // jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
//        }
//    }
    // Add Kotlin source directories to AndroidSourceSet.kotlin
    sourceSets {
//        named("main") {
//            kotlin {
//                directories += "additionalSourceDirectory/kotlin"
//
//            }
//        }
        named("main") {
            kotlin .directories.add("additionalSourceDirectory/kotlin")
        }
//        named("debug") {
//            kotlin .directories.add("additionalSourceDirectory/kotlin")
//        }

    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
//    room {
//        schemaDirectory("$projectDir/schemas")
////        schemaDirectory("/schemas")
//    }
}

//room {
//    schemaDirectory("$projectDir/schemas")
//}

kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
        // Optional: Set jvmTarget
         jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.compose.material)
//    implementation(libs.androidx.room.compiler)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.ui.unit)
//    implementation(libs.androidx.lifecycle.service)// This triggers the error: migrate the Indication implementation to implement IndicationNodeFactory
//    implementation(libs.androidx.compose.adaptive)// This triggers the error: migrate the Indication implementation to implement IndicationNodeFactory
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.compose.foundation)

    //Room
//    implementation(libs.androidx.room.runtime)
//    implementation(libs.androidx.room.ktx)

//    ksp(libs.androidx.room.compiler.v284)
//    ksp("androidx.room:room-compiler:2.5.0")
//    ksp("androidx.room:room-compiler:2.8.4")

    // View Model
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    // Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.session)

    // Permission management
    implementation(libs.accompanist.permissions)

    // Dagger - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Window size
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(libs.androidx.material3.adaptive)
//    implementation(libs.androidx.material3.adaptive.navigation.suite)
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")

//    // Supabase
//    implementation(platform("io.github.jan-tennert.supabase:bom:3.4.1"))
//    implementation("io.github.jan-tennert.supabase:postgrest-kt")
//    implementation("io.ktor:ktor-client-android:3.4.1")
////    implementation("oi.github.jan-tennert.supabase:auth-kt")

    // Google ads
    implementation("com.google.android.gms:play-services-ads:25.0.0")

    // Material 3
    implementation(platform("androidx.compose:compose-bom:2026.03.00"))
    implementation("androidx.compose.material3:material3")
    implementation ("androidx.compose.material:material-icons-extended")

    // PowerSync
    implementation(libs.powersync.core)

    // Dotenv
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    // PostHog
    implementation("com.posthog:posthog-android:3.+")

    // Encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
}

configurations.implementation{
    exclude(group = "com.intellij", module = "annotations")
}