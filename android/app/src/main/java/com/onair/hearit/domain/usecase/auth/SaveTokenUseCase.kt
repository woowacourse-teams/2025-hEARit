package com.onair.hearit.domain.usecase.auth

import com.onair.hearit.domain.repository.AuthRepository

class SaveTokenUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        accessToken: String,
        refreshToken: String,
    ): Result<Unit> =
        runCatching {
            authRepository.saveToken(accessToken).getOrThrow()
            authRepository.saveRefreshToken(refreshToken).getOrThrow()
        }.recoverCatching { throwable ->
            // 부분 성공 시 롤백
            authRepository.clearAuthData()
            throw throwable
        }
}
