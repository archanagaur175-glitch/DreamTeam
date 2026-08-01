// DreamTeam — root build configuration.
// AGP 9.x provides built-in Kotlin support, so the legacy
// `org.jetbrains.kotlin.android` plugin is intentionally NOT applied.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
}
