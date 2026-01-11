package com.onair.hearit.data.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.onair.hearit.di.UserPreferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class NotificationLocalDataSourceImpl @Inject constructor(
    @UserPreferencesDataStore
    private val dataStore: DataStore<Preferences>,
) : NotificationLocalDataSource {
    override suspend fun getCommutePushEnabled(): Result<Boolean> =
        runCatching {
            val preferences: Preferences = dataStore.data.first()
            preferences[IS_COMMUTE_PUSH_ENABLED_KEY] ?: DEFAULT_IS_COMMUTE_PUSH_ENABLED
        }

    override suspend fun saveCommutePushEnabled(isEnabled: Boolean): Result<Unit> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[IS_COMMUTE_PUSH_ENABLED_KEY] = isEnabled
            }
        }

    companion object {
        private val IS_COMMUTE_PUSH_ENABLED_KEY = booleanPreferencesKey("is_commute_push_enabled")
        private const val DEFAULT_IS_COMMUTE_PUSH_ENABLED: Boolean = false
    }
}
