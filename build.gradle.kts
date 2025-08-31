// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false

    alias(libs.plugins.compose.compiler) apply false

    // Java annotations
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false

    // Dependency injection
    id("com.google.dagger.hilt.android") version "2.56.2" apply false

//    id("androidx.room") version "$room_version" apply false
    id("androidx.room") version "2.7.2" apply false
}