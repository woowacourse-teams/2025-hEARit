package com.onair.hearit.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceProvidesModule {
    @Provides
    @Singleton
    fun providePreferencesLocalDataSource(dataStore: DataStore<Preferences>): PreferencesLocalDataSource =
        PreferencesLocalDataSourceImpl(dataStore)
}
