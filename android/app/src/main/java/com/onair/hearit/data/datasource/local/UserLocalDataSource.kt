package com.onair.hearit.data.datasource.local

import com.onair.hearit.domain.model.UserInfo

interface UserLocalDataSource {
    suspend fun getDeviceId(): Result<String>

    suspend fun saveDeviceId(userId: String): Result<Unit>

    suspend fun getUserInfo(): Result<UserInfo>

    suspend fun saveUserInfo(userInfo: UserInfo): Result<Unit>

    suspend fun clearData(): Result<Unit>
}
