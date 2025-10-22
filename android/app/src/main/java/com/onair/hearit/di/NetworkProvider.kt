package com.onair.hearit.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.onair.hearit.BuildConfig
import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.api.BookmarkService
import com.onair.hearit.data.api.CategoryService
import com.onair.hearit.data.api.HearitService
import com.onair.hearit.data.api.MediaFileService
import com.onair.hearit.data.api.MemberService
import com.onair.hearit.data.api.PlayingHistoryService
import com.onair.hearit.data.api.RecommendationService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create

object NetworkProvider {
    private val contentType = "application/json".toMediaType()

    private val logging by lazy { LoggingInterceptorProvider.provide() }

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    private val noAuthOkHttp: OkHttpClient by lazy {
        OkHttpClient
            .Builder()
            .addInterceptor(logging)
            .build()
    }

    private val authOkHttp: OkHttpClient by lazy {
        OkHttpClient
            .Builder()
            .addInterceptor(TokenInterceptorProvider.provide())
            .addInterceptor(logging)
            .apply {
                TokenAuthenticatorProvider.provide()?.let { authenticator(it) }
            }.build()
    }

    private val retrofitNoAuth: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(noAuthOkHttp)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    private val retrofitAuth: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(authOkHttp)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val authServiceNoAuth: AuthService by lazy { retrofitNoAuth.create() }

    val categoryService: CategoryService by lazy { retrofitAuth.create() }

    val hearitService: HearitService by lazy { retrofitAuth.create() }

    val mediaFileService: MediaFileService by lazy { retrofitAuth.create() }

    val bookmarkService: BookmarkService by lazy { retrofitAuth.create() }

    val memberService: MemberService by lazy { retrofitAuth.create() }

    val playingHistoryService: PlayingHistoryService by lazy { retrofitAuth.create() }

    val recommendationService: RecommendationService by lazy { retrofitAuth.create() }
}
