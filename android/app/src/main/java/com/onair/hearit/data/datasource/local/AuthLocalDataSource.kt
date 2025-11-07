package com.onair.hearit.data.datasource.local

interface AuthLocalDataSource {
    suspend fun getAccessToken(): Result<String>

    suspend fun getRefreshToken(): Result<String>

    suspend fun saveAccessToken(accessToken: String): Result<Unit>

    suspend fun saveRefreshToken(refreshToken: String): Result<Unit>

    suspend fun clearAuthData(): Result<Unit>
}
