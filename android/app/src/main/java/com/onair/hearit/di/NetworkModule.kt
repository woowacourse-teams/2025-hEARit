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
    fun provideTokenAuthenticator(
        preferencesLocalDataSource: PreferencesLocalDataSource,
        authService: AuthService,
    ): TokenAuthenticator =
        TokenAuthenticator(
            { preferencesLocalDataSource },
            { authService },
        )

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenAuthenticator: TokenAuthenticator): OkHttpClient {
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
    fun provideRetrofit(
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
    fun provideAuthService(retrofit: Retrofit): AuthService = retrofit.create()

    @Provides
    @Singleton
    fun provideCategoryService(retrofit: Retrofit): CategoryService = retrofit.create()

    @Provides
    @Singleton
    fun provideHearitService(retrofit: Retrofit): HearitService = retrofit.create()

    @Provides
    @Singleton
    fun provideMediaFileService(retrofit: Retrofit): MediaFileService = retrofit.create()

    @Provides
    @Singleton
    fun provideBookmarkService(retrofit: Retrofit): BookmarkService = retrofit.create()

    @Provides
    @Singleton
    fun provideMemberService(retrofit: Retrofit): MemberService = retrofit.create()

    @Provides
    @Singleton
    fun providePlayingHistoryService(retrofit: Retrofit): PlayingHistoryService = retrofit.create()

    @Provides
    @Singleton
    fun provideRecommendationService(retrofit: Retrofit): RecommendationService = retrofit.create()
}
