package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.AuthRemoteDataSource
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.TokenReissueRequest
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.LoginToken
import com.onair.hearit.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun kakaoLogin(accessToken: String): Result<LoginToken> =
        authRemoteDataSource
            .kakaoLogin(KakaoLoginRequest(accessToken))
            .mapOrThrowDomain { it.toDomain() }

    override suspend fun reissue(refreshToken: String): Result<String> =
        authRemoteDataSource
            .refreshAccessToken(TokenReissueRequest(refreshToken))
            .mapOrThrowDomain { it.accessToken }

    override suspend fun checkAccessToken(accessToken: String): Result<Unit> =
        authRemoteDataSource
            .checkAccessToken(accessToken)
            .mapOrThrowDomain { }
}
