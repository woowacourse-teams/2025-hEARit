package com.onair.hearit.domain.repository

interface AuthRepository {
    suspend fun login(accessToken: String): Result<String>
}
