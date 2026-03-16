package com.onair.hearit.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationPreferenceRepository {
    fun observeCommutePushEnabled(): Flow<Boolean>

    suspend fun saveCommutePushEnabled(isEnabled: Boolean): Result<Unit>

    suspend fun hasShownNotificationSuggestion(): Result<Boolean>

    suspend fun setNotificationSuggestionShown(): Result<Unit>
}
