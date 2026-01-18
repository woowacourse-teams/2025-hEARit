package com.onair.hearit.presentation.explore.di

import com.onair.hearit.presentation.explore.ExplorePlayerManager
import com.onair.hearit.presentation.explore.ExplorePlayerManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExploreModule {
    @Binds
    @Singleton
    abstract fun bindExplorePlayerManager(explorePlayerManagerImpl: ExplorePlayerManagerImpl): ExplorePlayerManager
}
