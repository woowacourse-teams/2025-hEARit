package com.onair.hearit.di

import com.onair.hearit.data.repository.AdvertisementRepositoryImpl
import com.onair.hearit.data.repository.AuthRepositoryImpl
import com.onair.hearit.data.repository.BookmarkRepositoryImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
import com.onair.hearit.data.repository.ExploreRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.LikeRepositoryImpl
import com.onair.hearit.data.repository.MediaFileRepositoryImpl
import com.onair.hearit.data.repository.NotificationPreferenceRepositoryImpl
import com.onair.hearit.data.repository.PlayingHistoryRepositoryImpl
import com.onair.hearit.data.repository.RecentHearitRepositoryImpl
import com.onair.hearit.data.repository.RecentKeywordRepositoryImpl
import com.onair.hearit.data.repository.RecommendationRepositoryImpl
import com.onair.hearit.data.repository.UserRepositoryImpl
import com.onair.hearit.domain.repository.AdvertisementRepository
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.domain.repository.ExploreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.LikeRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.repository.NotificationPreferenceRepository
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.domain.repository.RecommendationRepository
import com.onair.hearit.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAdvertisementRepository(advertisementRepositoryImpl: AdvertisementRepositoryImpl): AdvertisementRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(bookmarkRepositoryImpl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(categoryRepositoryImpl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindExploreRepository(exploreRepositoryImpl: ExploreRepositoryImpl): ExploreRepository

    @Binds
    @Singleton
    abstract fun bindHearitRepository(hearitRepositoryImpl: HearitRepositoryImpl): HearitRepository

    @Binds
    @Singleton
    abstract fun bindMediaFileRepository(mediaFileRepositoryImpl: MediaFileRepositoryImpl): MediaFileRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindRecentHearitRepository(recentHearitRepositoryImpl: RecentHearitRepositoryImpl): RecentHearitRepository

    @Binds
    @Singleton
    abstract fun bindRecentKeywordRepository(recentKeywordRepositoryImpl: RecentKeywordRepositoryImpl): RecentKeywordRepository

    @Binds
    @Singleton
    abstract fun bindPlayingHistoryRepository(playingHistoryRepositoryImpl: PlayingHistoryRepositoryImpl): PlayingHistoryRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(recommendationRepositoryImpl: RecommendationRepositoryImpl): RecommendationRepository

    @Binds
    @Singleton
    abstract fun bindNotificationPreferenceRepository(
        notificationPreferenceRepositoryImpl: NotificationPreferenceRepositoryImpl,
    ): NotificationPreferenceRepository

    @Binds
    @Singleton
    abstract fun bindLikeRepository(likeRepositoryImpl: LikeRepositoryImpl): LikeRepository
}
