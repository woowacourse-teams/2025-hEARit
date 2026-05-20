package com.onair.hearit.widget

import android.content.Context
import com.onair.hearit.data.AuthHeaderProvider
import com.onair.hearit.data.datasource.local.AuthLocalDataSource
import com.onair.hearit.domain.repository.BookmarkRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HearitWidgetEntryPoint {
    fun bookmarkRepository(): BookmarkRepository

    fun authLocalDataSource(): AuthLocalDataSource

    fun authHeaderProvider(): AuthHeaderProvider
}

fun Context.hearitWidgetEntryPoint(): HearitWidgetEntryPoint =
    EntryPointAccessors.fromApplication(
        applicationContext,
        HearitWidgetEntryPoint::class.java,
    )
