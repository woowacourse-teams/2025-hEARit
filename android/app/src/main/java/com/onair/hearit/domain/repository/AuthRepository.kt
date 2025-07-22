package com.onair.hearit.domain.repository

interface AuthRepository {
    suspend fun kakaoLogin(accessToken: String): Result<String>
}
