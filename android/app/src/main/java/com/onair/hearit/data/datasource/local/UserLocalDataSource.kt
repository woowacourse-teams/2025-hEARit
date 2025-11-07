package com.onair.hearit.data.datasource.local

import com.onair.hearit.domain.model.UserInfo

interface UserLocalDataSource {
    suspend fun getUserId(): Result<String>

    suspend fun saveUserId(userId: String): Result<Boolean>

    suspend fun getUserInfo(): Result<UserInfo>

    suspend fun saveUserInfo(userInfo: UserInfo): Result<Boolean>

    suspend fun clearData(): Result<Boolean>
}
