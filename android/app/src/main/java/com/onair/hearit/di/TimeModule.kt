package com.onair.hearit.di

import android.os.SystemClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ElapsedRealtime

@Module
@InstallIn(SingletonComponent::class)
class TimeModule {
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    @ElapsedRealtime
    fun provideElapsedRealtime(): () -> Long = { SystemClock.elapsedRealtime() }
}
