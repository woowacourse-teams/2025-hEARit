package com.onair.hearit.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.onair.hearit.BuildConfig
import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.api.HearitService
import com.onair.hearit.data.api.MediaFileService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create

object NetworkProvider {
    private val contentType = "application/json".toMediaType()

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    private val okhttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(LoggingInterceptorProvider.provide())
            .build()

    private val retrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okhttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val authService: AuthService by lazy { retrofit.create() }

    val hearitService: HearitService by lazy { retrofit.create() }
    val mediaFileService: MediaFileService by lazy { retrofit.create() }
}
