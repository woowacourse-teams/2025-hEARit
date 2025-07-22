package com.onair.hearit.data.repository

import android.content.Context
import com.onair.hearit.data.dataStore
import com.onair.hearit.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.first

class DataStoreRepositoryImpl(
    context: Context,
) : DataStoreRepository {
    private val dataStore: DataStore<Preferences> = context.dataStore

    override suspend fun getAccessToken(): Result<String> =
        handleResult {
            val preferences = dataStore.data.first()
            preferences[ACCESS_TOKEN_KEY] ?: throw IllegalStateException("access token이 존재하지 않습니다.")
        }

    override suspend fun saveAccessToken(accessToken: String): Result<Boolean> =
        handleResult {
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
            }
            true
        }

    override suspend fun clearData(): Result<Boolean> =
        handleResult {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            true
        }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    }
}
