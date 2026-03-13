// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.jetbrains.kotlin.android) apply false

    alias(libs.plugins.compose.compiler) apply false

//    id("com.android.application") version "9.1.0" apply false
//    id("com.android.library") version "9.1.0" apply false
//    id("org.jetbrains.kotlin.android") version "2.3.10" apply false

    // Java annotations
//    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    id("com.google.devtools.ksp") version "2.3.4" apply false

    // Dependency injection
    id("com.google.dagger.hilt.android") version "2.59.2" apply false

//    id("androidx.room") version "$room_version" apply false
    id("androidx.room") version "2.8.4" apply false
}