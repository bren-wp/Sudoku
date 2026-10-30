plugins {
    id("com.android.application") version "9.5.0" apply false
    // AGP 9 includes Kotlin support: do not apply org.jetbrains.kotlin.android.
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}
