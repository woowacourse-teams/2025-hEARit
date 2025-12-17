package com.onair.hearit.data.datasource.local

interface NotificationLocalDataSource {
    suspend fun getIsCommutePushEnabled(): Result<Boolean>

    suspend fun saveIsCommutePushEnabled(isEnabled: Boolean): Result<Unit>
}
