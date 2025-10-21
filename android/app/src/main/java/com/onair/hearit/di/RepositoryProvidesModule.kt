package com.onair.hearit.di

import android.content.Context
import com.onair.hearit.data.repository.ExploreDataStoreRepositoryImpl
import com.onair.hearit.domain.repository.ExploreDataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryProvidesModule {
    @Provides
    @Singleton
    fun provideExploreDataStoreRepository(
        @ApplicationContext context: Context,
    ): ExploreDataStoreRepository = ExploreDataStoreRepositoryImpl(context = context)
}
