package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.UserInfo

interface DataStoreRepository {
    suspend fun getAccessToken(): Result<String>

    suspend fun saveAccessToken(accessToken: String): Result<Boolean>

    suspend fun getUserInfo(): Result<UserInfo>

    suspend fun saveUserInfo(userInfo: UserInfo): Result<Boolean>

    suspend fun clearUserInfo(): Result<Boolean>

    suspend fun clearData(): Result<Boolean>
}
