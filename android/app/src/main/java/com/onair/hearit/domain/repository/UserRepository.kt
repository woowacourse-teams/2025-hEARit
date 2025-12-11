package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.UserInfo

interface UserRepository {
    fun getCachedUserInfo(): UserInfo?

    suspend fun getUserInfo(): Result<UserInfo>

    suspend fun getOrCreateDeviceId(): Result<String>

    suspend fun clearUserData(): Result<Unit>
}
