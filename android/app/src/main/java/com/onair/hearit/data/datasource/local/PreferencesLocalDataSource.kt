package com.onair.hearit.data.datasource.local

import com.onair.hearit.domain.model.UserInfo

interface PreferencesLocalDataSource {
    suspend fun getAccessToken(): Result<String>

    suspend fun getRefreshToken(): Result<String>

    suspend fun saveAccessToken(accessToken: String): Result<Boolean>

    suspend fun saveRefreshToken(refreshToken: String): Result<Boolean>

    suspend fun getUserInfo(): Result<UserInfo>

    suspend fun saveUserInfo(userInfo: UserInfo): Result<Boolean>

    suspend fun clearData(): Result<Boolean>
}
