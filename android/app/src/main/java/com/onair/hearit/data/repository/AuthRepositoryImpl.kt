package com.onair.hearit.data.repository

import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.datasource.AuthRemoteDataSource
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val crashlyticsLogger: CrashlyticsLogger,
) : AuthRepository {
    override suspend fun kakaoLogin(accessToken: String): Result<String> =
        handleResult(crashlyticsLogger) {
            val response =
                authRemoteDataSource.kakaoLogin(KakaoLoginRequest(accessToken)).getOrThrow()
            response.accessToken
        }
}
