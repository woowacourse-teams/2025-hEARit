package com.onair.hearit.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.api.BookmarkService
import com.onair.hearit.data.api.CategoryService
import com.onair.hearit.data.api.HearitService
import com.onair.hearit.data.api.MediaFileService
import com.onair.hearit.data.api.MemberService
import com.onair.hearit.data.api.PlayingHistoryService
import com.onair.hearit.data.api.RecommendationService
import com.onair.hearit.data.database.HearitDao
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSourceImpl
import com.onair.hearit.data.datasource.remote.AuthRemoteDataSource
import com.onair.hearit.data.datasource.remote.AuthRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSource
import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.CategoryRemoteDataSource
import com.onair.hearit.data.datasource.remote.CategoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSource
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.MediaFileRemoteDataSource
import com.onair.hearit.data.datasource.remote.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.MemberRemoteDataSource
import com.onair.hearit.data.datasource.remote.MemberRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.PlayingHistoryDataSource
import com.onair.hearit.data.datasource.remote.PlayingHistoryDataSourceImpl
import com.onair.hearit.data.datasource.remote.RecommendationRemoteDataSource
import com.onair.hearit.data.datasource.remote.RecommendationRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(
        authService: AuthService,
        errorHandler: ErrorResponseHandler,
    ): AuthRemoteDataSource = AuthRemoteDataSourceImpl(authService, errorHandler)

    @Provides
    @Singleton
    fun provideBookmarkDataSource(
        bookmarkService: BookmarkService,
        errorHandler: ErrorResponseHandler,
    ): BookmarkRemoteDataSource = BookmarkRemoteDataSourceImpl(bookmarkService, errorHandler)

    @Provides
    @Singleton
    fun provideCategoryRemoteDataSource(
        categoryService: CategoryService,
        errorHandler: ErrorResponseHandler,
    ): CategoryRemoteDataSource = CategoryRemoteDataSourceImpl(categoryService, errorHandler)

    @Provides
    @Singleton
    fun provideHearitRemoteDataSource(
        hearitService: HearitService,
        errorHandler: ErrorResponseHandler,
    ): HearitRemoteDataSource = HearitRemoteDataSourceImpl(hearitService, errorHandler)

    @Provides
    @Singleton
    fun provideMediaFileRemoteDataSource(
        mediaFileService: MediaFileService,
        errorHandler: ErrorResponseHandler,
    ): MediaFileRemoteDataSource = MediaFileRemoteDataSourceImpl(mediaFileService, errorHandler)

    @Provides
    @Singleton
    fun provideMemberRemoteDataSource(
        memberService: MemberService,
        errorHandler: ErrorResponseHandler,
    ): MemberRemoteDataSource = MemberRemoteDataSourceImpl(memberService, errorHandler)

    @Provides
    @Singleton
    fun providePlayingHistoryRemoteDataSource(
        playingHistoryService: PlayingHistoryService,
        errorHandler: ErrorResponseHandler,
    ): PlayingHistoryDataSource = PlayingHistoryDataSourceImpl(playingHistoryService, errorHandler)

    @Provides
    @Singleton
    fun provideRecommendationRemoteDataSource(
        recommendationService: RecommendationService,
        errorHandler: ErrorResponseHandler,
    ): RecommendationRemoteDataSource = RecommendationRemoteDataSourceImpl(recommendationService, errorHandler)

    @Provides
    @Singleton
    fun provideHearitLocalDataSource(hearitDao: HearitDao): HearitLocalDataSource = HearitLocalDataSourceImpl(hearitDao)

    @Provides
    @Singleton
    fun providePreferencesLocalDataSource(dataStore: DataStore<Preferences>): PreferencesLocalDataSource =
        PreferencesLocalDataSourceImpl(dataStore)
}
