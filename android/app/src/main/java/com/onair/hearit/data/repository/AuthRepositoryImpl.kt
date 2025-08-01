package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.AuthRemoteDataSource
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun kakaoLogin(accessToken: String): Result<String> =
        handleResult {
            val response =
                authRemoteDataSource.kakaoLogin(KakaoLoginRequest(accessToken)).getOrThrow()
            response.accessToken
        }
}
