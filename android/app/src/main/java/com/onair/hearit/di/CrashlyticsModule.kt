package com.onair.hearit.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.analytics.FirebaseCrashlyticsLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CrashlyticsModule {
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    @Singleton
    fun provideCrashlyticsLogger(firebaseCrashlytics: FirebaseCrashlytics): CrashlyticsLogger =
        FirebaseCrashlyticsLogger(firebaseCrashlytics)
}
