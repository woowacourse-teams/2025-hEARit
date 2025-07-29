package com.onair.hearit.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kakao.sdk.common.util.KakaoJson.json
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.dataStore
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.first

class DataStoreRepositoryImpl(
    context: Context,
    private val crashlyticsLogger: CrashlyticsLogger,
) : DataStoreRepository {
    private val dataStore: DataStore<Preferences> = context.dataStore

    override suspend fun getAccessToken(): Result<String> =
        handleResult(crashlyticsLogger) {
            val preferences = dataStore.data.first()
            preferences[ACCESS_TOKEN_KEY] ?: throw IllegalStateException("access token이 존재하지 않습니다.")
        }

    override suspend fun saveAccessToken(accessToken: String): Result<Boolean> =
        handleResult(crashlyticsLogger) {
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
            }
            true
        }

    override suspend fun getUserInfo(): Result<UserInfo> =
        handleResult(crashlyticsLogger) {
            val prefs = dataStore.data.first()
            val jsonString =
                prefs[USER_INFO_KEY]
                    ?: throw IllegalStateException("UserInfo가 존재하지 않습니다.")
            json.decodeFromString<UserInfo>(jsonString)
        }

    override suspend fun saveUserInfo(userInfo: UserInfo): Result<Boolean> =
        handleResult(crashlyticsLogger) {
            val jsonString = json.encodeToString(userInfo)
            dataStore.edit { prefs ->
                prefs[USER_INFO_KEY] = jsonString
            }
            true
        }

    override suspend fun clearData(): Result<Boolean> =
        handleResult(crashlyticsLogger) {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            true
        }

    override suspend fun clearUserInfo(): Result<Boolean> =
        handleResult(crashlyticsLogger) {
            dataStore.edit { prefs ->
                prefs.remove(USER_INFO_KEY)
            }
            true
        }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val USER_INFO_KEY = stringPreferencesKey("user_info_json")
    }
}
