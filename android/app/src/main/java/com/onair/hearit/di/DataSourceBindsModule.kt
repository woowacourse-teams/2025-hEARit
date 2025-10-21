package com.onair.hearit.di

import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
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
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceBindsModule {
    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(impl: AuthRemoteDataSourceImpl): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindBookmarkDataSource(impl: BookmarkRemoteDataSourceImpl): BookmarkRemoteDataSource

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
    abstract fun bindMemberRemoteDataSource(impl: MemberRemoteDataSourceImpl): MemberRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindPlayingHistoryRemoteDataSource(impl: PlayingHistoryDataSourceImpl): PlayingHistoryDataSource

    @Binds
    @Singleton
    abstract fun bindRecommendationRemoteDataSource(impl: RecommendationRemoteDataSourceImpl): RecommendationRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindHearitLocalDataSource(impl: HearitLocalDataSourceImpl): HearitLocalDataSource
}
