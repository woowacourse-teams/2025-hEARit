package com.onair.hearit.di

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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideAuthService(): AuthService = NetworkProvider.authServiceNoAuth

    @Provides
    @Singleton
    fun provideBookmarkService(): BookmarkService = NetworkProvider.bookmarkService

    @Provides
    @Singleton
    fun provideCategoryService(): CategoryService = NetworkProvider.categoryService

    @Provides
    @Singleton
    fun provideHearitService(): HearitService = NetworkProvider.hearitService

    @Provides
    @Singleton
    fun provideMediaFileService(): MediaFileService = NetworkProvider.mediaFileService

    @Provides
    @Singleton
    fun provideMemberService(): MemberService = NetworkProvider.memberService

    @Provides
    @Singleton
    fun providePlayingHistoryService(): PlayingHistoryService = NetworkProvider.playingHistoryService

    @Provides
    @Singleton
    fun provideRecommendationService(): RecommendationService = NetworkProvider.recommendationService
}
