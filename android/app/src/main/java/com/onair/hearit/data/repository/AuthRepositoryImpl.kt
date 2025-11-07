package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.AuthLocalDataSource
import com.onair.hearit.data.datasource.remote.AuthRemoteDataSource
import com.onair.hearit.data.dto.KakaoLoginRequest
import com.onair.hearit.data.dto.TokenReissueRequest
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.LoginToken
import com.onair.hearit.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authLocalDataSource: AuthLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun checkAccessToken(accessToken: String): Result<Unit> =
        authRemoteDataSource.checkAccessToken(accessToken).mapOrThrowDomain { }

    override suspend fun getAccessToken(): Result<String> = authLocalDataSource.getAccessToken()

    override suspend fun getRefreshToken(): Result<String> = authLocalDataSource.getRefreshToken()

    override suspend fun getTokens(): Result<Pair<String, String>> =
        runCatching {
            val accessToken = authLocalDataSource.getAccessToken().getOrThrow()
            val refreshToken = authLocalDataSource.getRefreshToken().getOrThrow()
            accessToken to refreshToken
        }

    override suspend fun saveAccessToken(accessToken: String): Result<Unit> =
        runCatching { authLocalDataSource.saveAccessToken(accessToken).getOrThrow() }

    override suspend fun saveRefreshToken(refreshToken: String): Result<Unit> =
        runCatching { authLocalDataSource.saveRefreshToken(refreshToken).getOrThrow() }

    override suspend fun saveToken(accessToken: String): Result<Unit> =
        runCatching { authLocalDataSource.saveAccessToken(accessToken).getOrThrow() }

    override suspend fun kakaoLogin(accessToken: String): Result<LoginToken> =
        authRemoteDataSource
            .kakaoLogin(KakaoLoginRequest(accessToken))
            .mapOrThrowDomain { it.toDomain() }

    override suspend fun reissue(refreshToken: String): Result<String> =
        authRemoteDataSource
            .refreshAccessToken(TokenReissueRequest(refreshToken))
            .mapOrThrowDomain { it.accessToken }

    override suspend fun withdraw(): Result<Unit> = runCatching { authRemoteDataSource.withdraw() }

    override suspend fun clearAuthData(): Result<Unit> = runCatching { authLocalDataSource.clearAuthData().getOrThrow() }
}
