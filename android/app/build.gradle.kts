import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.serialization)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.onair.hearit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.onair.hearit"
        minSdk = 29
        targetSdk = 35
        versionCode = 1004
        versionName = "1.0.04"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    defaultConfig {
        val baseUrl = gradleLocalProperties(rootDir, providers).getProperty("BASE_URL") ?: ""
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

        val kakaoNativeKey =
            gradleLocalProperties(rootDir, providers).getProperty("KAKAO_NATIVE_KEY") ?: ""
        buildConfigField("String", "KAKAO_NATIVE_KEY", "\"$kakaoNativeKey\"")

        manifestPlaceholders["kakaoNativeKey"] = kakaoNativeKey
    }
    buildFeatures {
        buildConfig = true
        dataBinding = true
    }
    ktlint {
        debug = true
    }
    testOptions {
        animationsDisabled = true
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // android test
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    debugImplementation(libs.androidx.fragment.testing)

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

    // lottie
    implementation(libs.lottie)

    // flexbox
    implementation(libs.flexbox)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // kakao SDK
    implementation(libs.v2.user)

    // dataStore
    implementation(libs.androidx.datastore.preferences)

    // room
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    // coil
    implementation(libs.coil)
}
