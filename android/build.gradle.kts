// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.5" apply false
    id("com.google.android.gms.oss-licenses-plugin") version "0.10.7" apply false
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
}
