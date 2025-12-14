package com.onair.hearit.data.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.onair.hearit.di.AuthPreferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthLocalDataSourceImpl @Inject constructor(
    @AuthPreferencesDataStore
    private val dataStore: DataStore<Preferences>,
) : AuthLocalDataSource {
    override suspend fun getAccessToken(): Result<String> =
        runCatching {
            val preferences = dataStore.data.first()
            preferences[ACCESS_TOKEN_KEY] ?: throw IllegalStateException("access token이 존재하지 않습니다.")
        }

    override suspend fun getRefreshToken(): Result<String> =
        runCatching {
            val preferences = dataStore.data.first()
            preferences[REFRESH_TOKEN_KEY]
                ?: throw IllegalStateException("refresh token이 존재하지 않습니다.")
        }

    override suspend fun saveAccessToken(accessToken: String): Result<Unit> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
            }
        }

    override suspend fun saveRefreshToken(refreshToken: String): Result<Unit> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[REFRESH_TOKEN_KEY] = refreshToken
            }
        }

    override suspend fun clearAuthData(): Result<Unit> =
        runCatching {
            dataStore.edit { preferences ->
                preferences.remove(ACCESS_TOKEN_KEY)
                preferences.remove(REFRESH_TOKEN_KEY)
            }
        }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }
}
