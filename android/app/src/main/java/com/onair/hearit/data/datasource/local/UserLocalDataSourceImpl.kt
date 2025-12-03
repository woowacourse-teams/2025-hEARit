package com.onair.hearit.data.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.onair.hearit.di.UserPreferencesDataStore
import com.onair.hearit.domain.model.UserInfo
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserLocalDataSourceImpl @Inject constructor(
    @UserPreferencesDataStore
    private val dataStore: DataStore<Preferences>,
) : UserLocalDataSource {
    override suspend fun getDeviceId(): Result<String> =
        runCatching {
            val preferences = dataStore.data.first()
            preferences[DEVICE_ID_KEY]
                ?: throw IllegalStateException("user id가 존재하지 않습니다.")
        }

    override suspend fun saveDeviceId(userId: String): Result<Unit> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[DEVICE_ID_KEY] = userId
            }
        }

    override suspend fun getUserInfo(): Result<UserInfo> =
        runCatching {
            val preferences = dataStore.data.first()
            val userId = preferences[LOGGED_USER_ID_KEY]

            if (userId == null || userId == INVALID_USER_ID) {
                throw IllegalStateException("저장된 유저 정보가 없습니다")
            }

            UserInfo(
                id = userId,
                nickname = preferences[NICKNAME_KEY] ?: DEFAULT_NICKNAME,
                profileImage = preferences[PROFILE_URL_KEY]?.takeIf { it.isNotEmpty() },
            )
        }

    override suspend fun saveUserInfo(userInfo: UserInfo): Result<Unit> =
        runCatching {
            dataStore.edit { preferences ->
                preferences[LOGGED_USER_ID_KEY] = userInfo.id
                preferences[NICKNAME_KEY] = userInfo.nickname
                preferences[PROFILE_URL_KEY] = userInfo.profileImage ?: EMPTY_PROFILE_IMAGE
            }
        }

    override suspend fun clearData(): Result<Unit> =
        runCatching {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }

    companion object {
        // 디바이스 식별자 (앱 삭제 전까지 유지)
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")

        // 로그인 사용자 정보 (로그아웃 시 삭제)
        private val LOGGED_USER_ID_KEY = longPreferencesKey("logged_user_id")
        private val NICKNAME_KEY = stringPreferencesKey("nickname")
        private val PROFILE_URL_KEY = stringPreferencesKey("profile_url")

        private const val INVALID_USER_ID = -1L
        private const val DEFAULT_NICKNAME = "hEARit"
        private const val EMPTY_PROFILE_IMAGE = ""
    }
}
