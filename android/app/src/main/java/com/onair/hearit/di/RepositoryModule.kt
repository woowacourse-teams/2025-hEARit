package com.onair.hearit.di

import android.content.Context
import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.data.datasource.remote.AuthRemoteDataSource
import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSource
import com.onair.hearit.data.datasource.remote.CategoryRemoteDataSource
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSource
import com.onair.hearit.data.datasource.remote.MediaFileRemoteDataSource
import com.onair.hearit.data.datasource.remote.MemberRemoteDataSource
import com.onair.hearit.data.datasource.remote.PlayingHistoryDataSource
import com.onair.hearit.data.datasource.remote.RecommendationRemoteDataSource
import com.onair.hearit.data.repository.AuthRepositoryImpl
import com.onair.hearit.data.repository.BookmarkRepositoryImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
import com.onair.hearit.data.repository.ExploreDataStoreRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.MediaFileRepositoryImpl
import com.onair.hearit.data.repository.MemberRepositoryImpl
import com.onair.hearit.data.repository.PlayingHistoryRepositoryImpl
import com.onair.hearit.data.repository.RecentHearitRepositoryImpl
import com.onair.hearit.data.repository.RecentKeywordRepositoryImpl
import com.onair.hearit.data.repository.RecommendationRepositoryImpl
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.domain.repository.ExploreDataStoreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.domain.repository.RecommendationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        authRemoteDataSource: AuthRemoteDataSource,
        preferencesLocalDataSource: PreferencesLocalDataSource,
    ): AuthRepository =
        AuthRepositoryImpl(
            authRemoteDataSource = authRemoteDataSource,
            preferencesLocalDataSource = preferencesLocalDataSource,
        )

    @Provides
    @Singleton
    fun provideBookmarkRepository(bookmarkRemoteDataSource: BookmarkRemoteDataSource): BookmarkRepository =
        BookmarkRepositoryImpl(bookmarkDataSource = bookmarkRemoteDataSource)

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryRemoteDataSource: CategoryRemoteDataSource): CategoryRepository =
        CategoryRepositoryImpl(categoryDataSource = categoryRemoteDataSource)

    @Provides
    @Singleton
    fun provideExploreDataStoreRepository(
        @ApplicationContext context: Context,
    ): ExploreDataStoreRepository = ExploreDataStoreRepositoryImpl(context = context)

    @Provides
    @Singleton
    fun provideHearitRepository(hearitRemoteDataSource: HearitRemoteDataSource): HearitRepository =
        HearitRepositoryImpl(hearitRemoteDataSource = hearitRemoteDataSource)

    @Provides
    @Singleton
    fun provideMediaFileRepository(mediaFileRemoteDataSource: MediaFileRemoteDataSource): MediaFileRepository =
        MediaFileRepositoryImpl(mediaFileRemoteDataSource = mediaFileRemoteDataSource)

    @Provides
    @Singleton
    fun provideMemberRepository(
        preferencesLocalDataSource: PreferencesLocalDataSource,
        memberRemoteDataSource: MemberRemoteDataSource,
    ): MemberRepository =
        MemberRepositoryImpl(
            preferencesLocalDataSource = preferencesLocalDataSource,
            memberRemoteDataSource = memberRemoteDataSource,
        )

    @Provides
    @Singleton
    fun providePlayingHistoryRepository(playingHistoryDataSource: PlayingHistoryDataSource): PlayingHistoryRepository =
        PlayingHistoryRepositoryImpl(playingHistoryDataSource = playingHistoryDataSource)

    @Provides
    @Singleton
    fun provideRecentHearitRepository(hearitLocalDataSource: HearitLocalDataSource): RecentHearitRepository =
        RecentHearitRepositoryImpl(hearitLocalDataSource = hearitLocalDataSource)

    @Provides
    @Singleton
    fun provideRecentKeywordRepository(hearitLocalDataSource: HearitLocalDataSource): RecentKeywordRepository =
        RecentKeywordRepositoryImpl(hearitLocalDataSource = hearitLocalDataSource)

    @Provides
    @Singleton
    fun provideRecommendationRepository(recommendationRemoteDataSource: RecommendationRemoteDataSource): RecommendationRepository =
        RecommendationRepositoryImpl(recommendationDataSource = recommendationRemoteDataSource)
}
