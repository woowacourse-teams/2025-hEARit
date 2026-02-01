package com.onair.hearit.presentation.explore.di

import androidx.lifecycle.ViewModel
import com.onair.hearit.presentation.explore.ExplorePlayerManager
import com.onair.hearit.presentation.explore.ExplorePlayerManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
abstract class ExploreModule {
    @Binds
    @ViewModelScoped
    abstract fun bindExplorePlayerManager(explorePlayerManagerImpl: ExplorePlayerManagerImpl): ExplorePlayerManager
}
