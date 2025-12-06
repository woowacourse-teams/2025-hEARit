package com.onair.hearit.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.analytics.FirebaseAnalyticsLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context,
    ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideAnalyticsLogger(firebaseAnalytics: FirebaseAnalytics): AnalyticsLogger = FirebaseAnalyticsLogger(firebaseAnalytics)
}
