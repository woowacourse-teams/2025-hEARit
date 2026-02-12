package com.onair.hearit.data.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.onair.hearit.di.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationLocalDataSourceImpl @Inject constructor(
    @UserPreferencesDataStore
    private val dataStore: DataStore<Preferences>,
) : NotificationLocalDataSource {
    override fun observeCommutePushEnabled(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[IS_COMMUTE_PUSH_ENABLED_KEY] ?: DEFAULT_COMMUTE_PUSH_ENABLED
        }

    override suspend fun saveCommutePushEnabled(enabled: Boolean): Result<Unit> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[IS_COMMUTE_PUSH_ENABLED_KEY] = enabled
            }
        }

    override suspend fun hasShownNotificationSuggestion(): Result<Boolean> =
        runCatching {
            val preferences = dataStore.data.first()
            preferences[HAS_SHOWN_NOTIFICATION_SUGGESTION_KEY] ?: false
        }

    override suspend fun setNotificationSuggestionShown(): Result<Unit> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[HAS_SHOWN_NOTIFICATION_SUGGESTION_KEY] = true
            }
        }

    companion object {
        private val IS_COMMUTE_PUSH_ENABLED_KEY =
            booleanPreferencesKey("is_commute_push_enabled")

        private val HAS_SHOWN_NOTIFICATION_SUGGESTION_KEY =
            booleanPreferencesKey("has_shown_notification_suggestion")
        private const val DEFAULT_COMMUTE_PUSH_ENABLED: Boolean = false
    }
}
