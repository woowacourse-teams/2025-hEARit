package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.AuthRemoteDataSource
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun kakaoLogin(accessToken: String): Result<String> =
        authRemoteDataSource
            .kakaoLogin(KakaoLoginRequest(accessToken))
            .mapOrThrowDomain { it.accessToken }
}
