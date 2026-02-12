package com.onair.hearit.data.datasource.local

import kotlinx.coroutines.flow.Flow

interface NotificationLocalDataSource {
    fun observeCommutePushEnabled(): Flow<Boolean>

    suspend fun saveCommutePushEnabled(enabled: Boolean): Result<Unit>

    suspend fun hasShownNotificationSuggestion(): Result<Boolean>

    suspend fun setNotificationSuggestionShown(): Result<Unit>
}
