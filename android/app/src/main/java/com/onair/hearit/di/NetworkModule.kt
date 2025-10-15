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
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val CONTENT_TYPE = "application/json"

    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        preferencesLocalDataSource: PreferencesLocalDataSource,
        @RefreshRetrofit refreshAuthService: AuthService,
    ): OkHttpClient {
        val tokenAuthenticator =
            TokenAuthenticator(
                preferencesLocalDataSource,
                { refreshAuthService },
            )

        val builder =
            OkHttpClient
                .Builder()
                .addInterceptor(TokenInterceptorProvider.provide())
                .addInterceptor(LoggingInterceptorProvider.provide())
                .authenticator(tokenAuthenticator)

        return builder.build()
    }

    @Provides
    @Singleton
    @NormalRetrofit
    fun provideNormalRetrofit(
        json: Json,
        client: OkHttpClient,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(CONTENT_TYPE.toMediaType()))
            .build()

    @Provides
    @Singleton
    @RefreshRetrofit
    fun provideRefreshRetrofit(json: Json): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(json.asConverterFactory(CONTENT_TYPE.toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideNormalAuthService(
        @NormalRetrofit retrofit: Retrofit,
    ): AuthService = retrofit.create()

    @Provides
    @Singleton
    @RefreshRetrofit
    fun provideRefreshAuthService(
        @RefreshRetrofit retrofit: Retrofit,
    ): AuthService = retrofit.create()

    @Provides
    @Singleton
    fun provideCategoryService(
        @NormalRetrofit retrofit: Retrofit,
    ): CategoryService = retrofit.create()

    @Provides
    @Singleton
    fun provideHearitService(
        @NormalRetrofit retrofit: Retrofit,
    ): HearitService = retrofit.create()

    @Provides
    @Singleton
    fun provideMediaFileService(
        @NormalRetrofit retrofit: Retrofit,
    ): MediaFileService = retrofit.create()

    @Provides
    @Singleton
    fun provideBookmarkService(
        @NormalRetrofit retrofit: Retrofit,
    ): BookmarkService = retrofit.create()

    @Provides
    @Singleton
    fun provideMemberService(
        @NormalRetrofit retrofit: Retrofit,
    ): MemberService = retrofit.create()

    @Provides
    @Singleton
    fun providePlayingHistoryService(
        @NormalRetrofit retrofit: Retrofit,
    ): PlayingHistoryService = retrofit.create()

    @Provides
    @Singleton
    fun provideRecommendationService(
        @NormalRetrofit retrofit: Retrofit,
    ): RecommendationService = retrofit.create()
}
