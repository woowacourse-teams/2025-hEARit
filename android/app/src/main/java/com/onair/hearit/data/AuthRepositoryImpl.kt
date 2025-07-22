package com.onair.hearit.data

import com.onair.hearit.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun login(accessToken: String): Result<String> =
        handleResult {
            val response = authRemoteDataSource.login(LoginRequest(accessToken)).getOrThrow()
            response.accessToken
        }
}
