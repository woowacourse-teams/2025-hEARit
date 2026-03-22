import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.google.firebase.crashlytics")
    id("com.google.android.gms.oss-licenses-plugin")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.onair.hearit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.onair.hearit"
        minSdk = 29
        targetSdk = 35
        versionCode = 10500
        versionName = "1.5.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunner = "com.onair.hearit.HearitTestRunner"

        manifestPlaceholders += mapOf()
        val kakaoNativeKey =
            gradleLocalProperties(rootDir, providers).getProperty("KAKAO_NATIVE_KEY") ?: ""
        buildConfigField("String", "KAKAO_NATIVE_KEY", "\"$kakaoNativeKey\"")
        manifestPlaceholders["kakaoNativeKey"] = kakaoNativeKey
    }

    val signingFile = rootProject.file("keystore.properties")
    val releaseSigningConfig =
        if (signingFile.exists()) {
            val keystoreProperties =
                Properties().apply {
                    load(FileInputStream(signingFile))
                }

            signingConfigs.create("release") {
                storeFile = file("${keystoreProperties["store_file"]}")
                keyAlias = "${keystoreProperties["key_alias"]}"
                keyPassword = "${keystoreProperties["key_password"]}"
                storePassword = "${keystoreProperties["keystore_password"]}"
            }
        } else {
            null
        }

    applicationVariants.all {
        outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            val newFileName = "hEARit-$name.apk"
            outputImpl.outputFileName = newFileName
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            val baseUrl =
                gradleLocalProperties(rootDir, providers).getProperty("BASE_URL") ?: ""
            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

            if (releaseSigningConfig != null) {
                signingConfig = releaseSigningConfig
            }
        }

        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-Dev"
            resValue("string", "app_name", "hEARit (Dev)")

            val devBaseUrl =
                gradleLocalProperties(rootDir, providers).getProperty("DEV_BASE_URL") ?: ""
            buildConfigField("String", "BASE_URL", "\"$devBaseUrl\"")

            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
        compose = true
    }

    ktlint {
        debug = true
    }

    testOptions {
        animationsDisabled = true
    }

    tasks.register("printVersionCode") {
        doLast {
            println(android.defaultConfig.versionCode)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2025.08.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.collections.immutable)

    // Preview
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    testImplementation(libs.androidx.core.testing)

    // test
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.assertj.core)
    testImplementation(kotlin("test"))

    // android test
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.turbine)
    debugImplementation(libs.androidx.fragment.testing)
    debugImplementation(libs.androidx.ui.test.manifest)

    // remote
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    // media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.concurrent.futures.ktx)

    // lottie
    implementation(libs.lottie)

    // flexbox
    implementation(libs.flexbox)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.messaging.ktx)

    // kakao SDK
    implementation(libs.v2.user)
    implementation(libs.v2.share)

    // dataStore
    implementation(libs.androidx.datastore.preferences)

    // room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // coil
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // timber
    implementation(libs.timber)

    // shimmer
    implementation(libs.shimmer)

    // in-app-update
    implementation(libs.app.update.ktx)

    // open license
    implementation(libs.play.services.oss.licenses)

    // compose navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    // hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // hilt test
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)

    // swipe Refresh Layout
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.lottie.compose)
}
