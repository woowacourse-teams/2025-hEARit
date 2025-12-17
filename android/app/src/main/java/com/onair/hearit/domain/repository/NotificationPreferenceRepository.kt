package com.onair.hearit.domain.repository

interface NotificationPreferenceRepository {
    fun getCachedCommutePushEnabled(): Boolean?

    suspend fun getIsCommutePushEnabled(): Result<Boolean>

    suspend fun saveIsCommutePushEnabled(isEnabled: Boolean): Result<Unit>
}
