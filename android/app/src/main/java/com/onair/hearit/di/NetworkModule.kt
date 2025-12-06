package com.onair.hearit.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.onair.hearit.BuildConfig
import com.onair.hearit.data.TokenAuthenticator
import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.api.BookmarkService
import com.onair.hearit.data.api.CategoryService
import com.onair.hearit.data.api.HearitService
import com.onair.hearit.data.api.MediaFileService
import com.onair.hearit.data.api.MemberService
import com.onair.hearit.data.api.PlayingHistoryService
import com.onair.hearit.data.api.RecommendationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
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
        }

    @Provides
    @Singleton
    @Named("logging")
    fun provideLoggingInterceptor(): Interceptor = LoggingInterceptorProvider.provide()

    @Provides
    @Singleton
    @Named("token")
    fun provideTokenInterceptor(): Interceptor = TokenInterceptorProvider.provide()

    @Provides
    @Singleton
    @Named("noAuth")
    fun provideOkHttpClientWithoutAuth(
        @Named("logging") loggingInterceptor: Interceptor,
    ): OkHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideOkHttpClientWithAuth(
        @Named("token") tokenInterceptor: Interceptor,
        @Named("logging") loggingInterceptor: Interceptor,
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
}
