package com.onair.hearit.data.datasource.local

interface NotificationLocalDataSource {
    suspend fun getCommutePushEnabled(): Result<Boolean>

    suspend fun saveCommutePushEnabled(enabled: Boolean): Result<Unit>

    suspend fun hasShownNotificationSuggestion(): Result<Boolean>

    suspend fun setNotificationSuggestionShown(): Result<Unit>
}
