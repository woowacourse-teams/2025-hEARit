package com.onair.hearit.domain.repository

interface DataStoreRepository {
    suspend fun getAccessToken(): Result<String>

    suspend fun saveAccessToken(accessToken: String): Result<Boolean>

    suspend fun clearData(): Result<Boolean>
}
