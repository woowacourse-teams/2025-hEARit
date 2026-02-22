package com.onair.hearit.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {
    companion object {
        private const val AUTH_PREFERENCES_NAME: String = "auth_prefs"
        private const val USER_PREFERENCES_NAME: String = "user_prefs"
        private const val EXPLORE_PREFERENCES_NAME: String = "explore_prefs"

        @AuthPreferencesDataStore
        @Provides
        @Singleton
        fun provideAuthPreferencesDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile(AUTH_PREFERENCES_NAME)
            }

        @UserPreferencesDataStore
        @Provides
        @Singleton
        fun provideUserPreferencesDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile(USER_PREFERENCES_NAME)
            }

        @ExplorePreferencesDataStore
        @Provides
        @Singleton
        fun provideExplorePreferencesDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile(EXPLORE_PREFERENCES_NAME)
            }
    }
}
