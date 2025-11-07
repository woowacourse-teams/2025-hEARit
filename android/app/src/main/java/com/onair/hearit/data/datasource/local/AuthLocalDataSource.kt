package com.onair.hearit.data.datasource.local

interface AuthLocalDataSource {
    suspend fun getAccessToken(): Result<String>

    suspend fun getRefreshToken(): Result<String>

    suspend fun saveAccessToken(accessToken: String): Result<Boolean>

    suspend fun saveRefreshToken(refreshToken: String): Result<Boolean>

    suspend fun clearAuthData(): Result<Boolean>
}
