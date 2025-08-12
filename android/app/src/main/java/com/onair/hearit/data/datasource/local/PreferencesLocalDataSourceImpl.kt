package com.onair.hearit.data.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.onair.hearit.domain.model.UserInfo
import kotlinx.coroutines.flow.first

class PreferencesLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : PreferencesLocalDataSource {
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

    override suspend fun saveAccessToken(accessToken: String): Result<Boolean> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
            }
            true
        }

    override suspend fun saveRefreshToken(refreshToken: String): Result<Boolean> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[REFRESH_TOKEN_KEY] = refreshToken
            }
            true
        }

    override suspend fun getUserInfo(): Result<UserInfo> =
        runCatching {
            val prefs = dataStore.data.first()
            UserInfo(
                id = prefs[USER_ID_KEY] ?: -1,
                nickname = prefs[NICKNAME_KEY] ?: "hEARit",
                profileImage = prefs[PROFILE_URL_KEY] ?: "",
            )
        }

    override suspend fun saveUserInfo(userInfo: UserInfo): Result<Boolean> =
        runCatching {
            dataStore.edit { prefs ->
                prefs[USER_ID_KEY] = userInfo.id
                prefs[NICKNAME_KEY] = userInfo.nickname
                prefs[PROFILE_URL_KEY] = userInfo.profileImage ?: ""
            }
            true
        }

    override suspend fun clearData(): Result<Boolean> =
        runCatching {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            true
        }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val NICKNAME_KEY = stringPreferencesKey("nickname")
        private val PROFILE_URL_KEY = stringPreferencesKey("profile_url")
    }
}
