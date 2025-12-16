package com.onair.hearit.di

import android.content.Context
import com.onair.hearit.data.database.HearitDao
import com.onair.hearit.data.database.HearitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): HearitDatabase = HearitDatabase.getInstance(context)

    @Provides
    fun provideDao(database: HearitDatabase): HearitDao = database.hearitDao()
}
