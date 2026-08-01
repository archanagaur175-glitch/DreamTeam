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

subprojects {
    configurations.configureEach {
        resolutionStrategy {
            // Hilt/Dagger bundle an older kotlin-metadata-jvm than Kotlin 2.4 requires,
            // so Dagger's processors fail with "Provided Metadata instance has version
            // 2.4.0, while maximum supported version is 2.3.0". Force the version that
            // matches our Kotlin so the Hilt KSP + javac processors can read the
            // metadata of Kotlin 2.4-compiled classes.
            force("org.jetbrains.kotlin:kotlin-metadata-jvm:${libs.versions.kotlin.get()}")
        }
    }
}
