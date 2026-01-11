package com.onair.hearit.data.datasource.local

interface NotificationLocalDataSource {
    suspend fun getCommutePushEnabled(): Result<Boolean>

    suspend fun saveCommutePushEnabled(isEnabled: Boolean): Result<Unit>
}
