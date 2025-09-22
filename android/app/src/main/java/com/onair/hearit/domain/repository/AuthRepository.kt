package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.LoginToken

interface AuthRepository {
    suspend fun checkAccessToken(accessToken: String): Result<Unit>

    suspend fun getTokens(): Result<Pair<String, String>>

    suspend fun saveToken(accessToken: String): Result<Unit>

    suspend fun kakaoLogin(accessToken: String): Result<LoginToken>

    suspend fun reissue(refreshToken: String): Result<String>

    suspend fun withdraw(): Result<Unit>
}
