package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.LoginToken

interface AuthRepository {
    suspend fun kakaoLogin(accessToken: String): Result<LoginToken>

    suspend fun reissue(refreshToken: String): Result<String>

    suspend fun checkAccessToken(accessToken: String): Result<Unit>
}
