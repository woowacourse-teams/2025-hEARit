package com.onair.hearit.di

import com.onair.hearit.data.repository.AuthRepositoryImpl
import com.onair.hearit.data.repository.BookmarkRepositoryImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
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
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.domain.repository.RecommendationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindsModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindHearitRepository(impl: HearitRepositoryImpl): HearitRepository

    @Binds
    @Singleton
    abstract fun bindMediaFileRepository(impl: MediaFileRepositoryImpl): MediaFileRepository

    @Binds
    @Singleton
    abstract fun bindMemberRepository(impl: MemberRepositoryImpl): MemberRepository

    @Binds
    @Singleton
    abstract fun bindPlayingHistoryRepository(impl: PlayingHistoryRepositoryImpl): PlayingHistoryRepository

    @Binds
    @Singleton
    abstract fun bindRecentHearitRepository(impl: RecentHearitRepositoryImpl): RecentHearitRepository

    @Binds
    @Singleton
    abstract fun bindRecentKeywordRepository(impl: RecentKeywordRepositoryImpl): RecentKeywordRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(impl: RecommendationRepositoryImpl): RecommendationRepository
}
