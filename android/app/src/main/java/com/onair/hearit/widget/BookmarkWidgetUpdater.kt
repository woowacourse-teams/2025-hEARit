package com.onair.hearit.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class BookmarkWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun refresh() {
        runCatching {
            HearitWidget().updateAll(context)
        }.onFailure { t ->
            Timber.w(t, "Failed to refresh bookmark widget")
        }
    }
}
