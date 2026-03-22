package com.onair.hearit.di

import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.local.AuthLocalDataSource
import com.onair.hearit.data.datasource.local.AuthLocalDataSourceImpl
import com.onair.hearit.data.datasource.local.ExploreLocalDataStore
import com.onair.hearit.data.datasource.local.ExploreLocalDataStoreImpl
import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.datasource.local.NotificationLocalDataSource
import com.onair.hearit.data.datasource.local.NotificationLocalDataSourceImpl
import com.onair.hearit.data.datasource.local.UserLocalDataSource
import com.onair.hearit.data.datasource.local.UserLocalDataSourceImpl
import com.onair.hearit.data.datasource.remote.AdvertisementRemoteDataSource
import com.onair.hearit.data.datasource.remote.AdvertisementRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.AuthRemoteDataSource
import com.onair.hearit.data.datasource.remote.AuthRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSource
import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.CategoryRemoteDataSource
import com.onair.hearit.data.datasource.remote.CategoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSource
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.LikeRemoteDataSource
import com.onair.hearit.data.datasource.remote.LikeRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.MediaFileRemoteDataSource
import com.onair.hearit.data.datasource.remote.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.PlayingHistoryRemoteDataSource
import com.onair.hearit.data.datasource.remote.PlayingHistoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.RecommendationRemoteDataSource
import com.onair.hearit.data.datasource.remote.RecommendationRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.UserRemoteDataSource
import com.onair.hearit.data.datasource.remote.UserRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindAdvertisementRemoteDataSource(impl: AdvertisementRemoteDataSourceImpl): AdvertisementRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(impl: AuthRemoteDataSourceImpl): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindBookmarkRemoteDataSource(impl: BookmarkRemoteDataSourceImpl): BookmarkRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCategoryRemoteDataSource(impl: CategoryRemoteDataSourceImpl): CategoryRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindHearitRemoteDataSource(impl: HearitRemoteDataSourceImpl): HearitRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindMediaFileRemoteDataSource(impl: MediaFileRemoteDataSourceImpl): MediaFileRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindUserRemoteDataSource(impl: UserRemoteDataSourceImpl): UserRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindPlayingHistoryRemoteDataSource(impl: PlayingHistoryRemoteDataSourceImpl): PlayingHistoryRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindRecommendationRemoteDataSource(impl: RecommendationRemoteDataSourceImpl): RecommendationRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindLikeRemoteDataSource(impl: LikeRemoteDataSourceImpl): LikeRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindHearitLocalDataSource(impl: HearitLocalDataSourceImpl): HearitLocalDataSource

    @Binds
    @Singleton
    abstract fun bindAuthLocalDataSource(impl: AuthLocalDataSourceImpl): AuthLocalDataSource

    @Binds
    @Singleton
    abstract fun bindUserLocalDataSource(impl: UserLocalDataSourceImpl): UserLocalDataSource

    @Binds
    @Singleton
    abstract fun bindNotificationLocalDataSource(impl: NotificationLocalDataSourceImpl): NotificationLocalDataSource

    @Binds
    @Singleton
    abstract fun bindExploreLocalDataSource(impl: ExploreLocalDataStoreImpl): ExploreLocalDataStore

    companion object {
        @Provides
        @Singleton
        fun provideErrorResponseHandler(): ErrorResponseHandler = ErrorResponseHandler()
    }
}
