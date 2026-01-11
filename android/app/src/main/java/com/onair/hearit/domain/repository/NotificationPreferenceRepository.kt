package com.onair.hearit.domain.repository

interface NotificationPreferenceRepository {
    fun getCachedCommutePushEnabled(): Boolean?

    suspend fun getCommutePushEnabled(): Result<Boolean>

    suspend fun saveCommutePushEnabled(isEnabled: Boolean): Result<Unit>
}
