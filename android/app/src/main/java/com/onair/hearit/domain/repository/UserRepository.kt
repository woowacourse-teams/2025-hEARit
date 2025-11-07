package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.UserInfo

interface UserRepository {
    suspend fun getUserInfo(): Result<UserInfo>

    suspend fun getOrCreateUserId(): Result<String>

    suspend fun clearUserData(): Result<Boolean>
}
