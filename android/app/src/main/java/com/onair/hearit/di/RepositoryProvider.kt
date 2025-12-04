package com.onair.hearit.di

import android.content.Context
import com.onair.hearit.data.repository.AuthRepositoryImpl
import com.onair.hearit.data.repository.BookmarkRepositoryImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
import com.onair.hearit.data.repository.ExploreDataStoreRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.MediaFileRepositoryImpl
import com.onair.hearit.data.repository.PlayingHistoryRepositoryImpl
import com.onair.hearit.data.repository.RecentHearitRepositoryImpl
import com.onair.hearit.data.repository.RecentKeywordRepositoryImpl
import com.onair.hearit.data.repository.RecommendationRepositoryImpl
import com.onair.hearit.data.repository.UserRepositoryImpl
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.domain.repository.ExploreDataStoreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.domain.repository.RecommendationRepository
import com.onair.hearit.domain.repository.UserRepository

object RepositoryProvider {
    private lateinit var appContext: Context

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            authLocalDataSource = DataSourceProvider.authLocalDataSource,
            authRemoteDataSource = DataSourceProvider.authRemoteDataSource,
        )
    }

    val bookmarkRepository: BookmarkRepository by lazy {
        BookmarkRepositoryImpl(
            bookmarkRemoteDataSource = DataSourceProvider.bookmarkRemoteDataSource,
        )
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(categoryRemoteDataSource = DataSourceProvider.categoryRemoteDataSource)
    }

    val exploreDataStoreRepository: ExploreDataStoreRepository by lazy {
        ExploreDataStoreRepositoryImpl(context = appContext)
    }

    val hearitRepository: HearitRepository by lazy {
        HearitRepositoryImpl(hearitRemoteDataSource = DataSourceProvider.hearitRemoteDataSource)
    }

    val mediaFileRepository: MediaFileRepository by lazy {
        MediaFileRepositoryImpl(mediaFileRemoteDataSource = DataSourceProvider.mediaFileRemoteDataSource)
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(
            userLocalDataSource = DataSourceProvider.userLocalDataSource,
            userRemoteDataSource = DataSourceProvider.userRemoteDataSource,
        )
    }

    val recentHearitRepository: RecentHearitRepository by lazy {
        RecentHearitRepositoryImpl(hearitLocalDataSource = DataSourceProvider.hearitLocalDataSource)
    }

    val recentKeywordRepository: RecentKeywordRepository by lazy {
        RecentKeywordRepositoryImpl(hearitLocalDataSource = DataSourceProvider.hearitLocalDataSource)
    }

    val playingHistoryRepository: PlayingHistoryRepository by lazy {
        PlayingHistoryRepositoryImpl(playingHistoryRemoteDataSource = DataSourceProvider.playingHistoryRemoteDataSource)
    }

    val recommendationRepository: RecommendationRepository by lazy {
        RecommendationRepositoryImpl(recommendationRemoteDataSource = DataSourceProvider.recommendationRemoteDataSource)
    }
}
