package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.UserInfo

interface UserRepository {
    suspend fun getUserInfo(): Result<UserInfo>

    suspend fun getUserId(): Result<String>

    suspend fun getOrCreateUserId(): Result<String>

    suspend fun saveUserId(userId: String): Result<Boolean>

    suspend fun clearUserData(): Result<Boolean>
}
