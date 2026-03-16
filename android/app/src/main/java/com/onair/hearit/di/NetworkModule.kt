package com.onair.hearit.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.onair.hearit.BuildConfig
import com.onair.hearit.data.TokenAuthenticator
import com.onair.hearit.data.TokenInterceptor
import com.onair.hearit.data.api.AdvertisementService
import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.api.BookmarkService
import com.onair.hearit.data.api.CategoryService
import com.onair.hearit.data.api.HearitService
import com.onair.hearit.data.api.LikeService
import com.onair.hearit.data.api.MediaFileService
import com.onair.hearit.data.api.MemberService
import com.onair.hearit.data.api.PlayingHistoryService
import com.onair.hearit.data.api.RecommendationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import timber.log.Timber
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val APPLICATION_JSON: String = "application/json"

    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(json: Json): HttpLoggingInterceptor =
        HttpLoggingInterceptor(
            object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    if (message.startsWith("{") || message.startsWith("[")) {
                        runCatching {
                            val parsed: JsonElement = json.parseToJsonElement(message)
                            val pretty: String =
                                json.encodeToString(JsonElement.serializer(), parsed)
                            Timber.i(pretty)
                        }.onFailure {
                            Timber.i(message)
                        }
                        return
                    }
                    Timber.i(message)
                }
            },
        ).apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }

    @Provides
    @Singleton
    @Named("noAuth")
    fun provideOkHttpClientWithoutAuth(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideOkHttpClientWithAuth(
        tokenInterceptor: TokenInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(tokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()

    @Provides
    @Singleton
    @Named("noAuth")
    fun provideRetrofitWithoutAuth(
        @Named("noAuth") okHttpClientWithoutAuth: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = APPLICATION_JSON.toMediaType()
        return Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClientWithoutAuth)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @Named("auth")
    fun provideRetrofitWithAuth(
        @Named("auth") okHttpClientWithAuth: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = APPLICATION_JSON.toMediaType()
        return Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClientWithAuth)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @Named("noAuth")
    fun provideAdvertisementServiceWithoutAuth(
        @Named("noAuth") retrofitWithoutAuth: Retrofit,
    ): AdvertisementService = retrofitWithoutAuth.create()

    @Provides
    @Singleton
    @Named("noAuth")
    fun provideAuthServiceWithoutAuth(
        @Named("noAuth") retrofitWithoutAuth: Retrofit,
    ): AuthService = retrofitWithoutAuth.create()

    @Provides
    @Singleton
    fun provideAuthServiceWithAuth(
        @Named("auth") retrofitWithAuth: Retrofit,
    ): AuthService = retrofitWithAuth.create()

    @Provides
    @Singleton
    fun provideCategoryService(
        @Named("auth") retrofitWithAuth: Retrofit,
    ): CategoryService = retrofitWithAuth.create()

    @Provides
    @Singleton
    fun provideHearitService(
        @Named("auth") retrofitWithAuth: Retrofit,
    ): HearitService = retrofitWithAuth.create()

    @Provides
    @Singleton
    fun provideMediaFileService(
        @Named("auth") retrofitWithAuth: Retrofit,
    ): MediaFileService = retrofitWithAuth.create()

    @Provides
    @Singleton
    fun provideBookmarkService(
        @Named("auth") retrofitWithAuth: Retrofit,
    ): BookmarkService = retrofitWithAuth.create()

    @Provides
    @Singleton
    fun provideMemberService(
        @Named("auth") retrofitWithAuth: Retrofit,
    ): MemberService = retrofitWithAuth.create()

    @Provides
    @Singleton
    fun providePlayingHistoryService(
        @Named("auth") retrofitWithAuth: Retrofit,
    ): PlayingHistoryService = retrofitWithAuth.create()

    @Provides
    @Singleton
    fun provideRecommendationService(
        @Named("auth") retrofitWithAuth: Retrofit,
    ): RecommendationService = retrofitWithAuth.create()

    @Provides
    @Singleton
    fun provideLikeService(
        @Named("auth") retrofitWithAuth: Retrofit,
    ): LikeService = retrofitWithAuth.create()
}
