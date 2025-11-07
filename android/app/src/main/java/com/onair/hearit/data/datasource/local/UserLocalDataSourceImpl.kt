package com.onair.hearit.data.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.onair.hearit.domain.model.UserInfo
import kotlinx.coroutines.flow.first

class UserLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : UserLocalDataSource {
    override suspend fun getUserId(): Result<String> =
        runCatching {
            val preferences = dataStore.data.first()
            preferences[USER_ID_KEY]
                ?: throw IllegalStateException("user id가 존재하지 않습니다.")
        }

    override suspend fun saveUserId(userId: String): Result<Boolean> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[USER_ID_KEY] = userId
            }
            true
        }

    override suspend fun getUserInfo(): Result<UserInfo> =
        runCatching {
            val prefs = dataStore.data.first()
            UserInfo(
                id = prefs[LOGGED_USER_ID_KEY] ?: -1,
                nickname = prefs[NICKNAME_KEY] ?: "hEARit",
                profileImage = prefs[PROFILE_URL_KEY] ?: "",
            )
        }

    override suspend fun saveUserInfo(userInfo: UserInfo): Result<Boolean> =
        runCatching {
            dataStore.edit { prefs ->
                prefs[LOGGED_USER_ID_KEY] = userInfo.id
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
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val LOGGED_USER_ID_KEY = longPreferencesKey("logged_user_id")
        private val NICKNAME_KEY = stringPreferencesKey("nickname")
        private val PROFILE_URL_KEY = stringPreferencesKey("profile_url")
    }
}
