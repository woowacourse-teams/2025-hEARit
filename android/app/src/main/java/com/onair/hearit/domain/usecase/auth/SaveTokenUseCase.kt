package com.onair.hearit.domain.usecase.auth

import com.onair.hearit.domain.repository.AuthRepository
import javax.inject.Inject

class SaveTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        accessToken: String,
        refreshToken: String,
    ): Result<Unit> =
        runCatching {
            authRepository.saveAccessToken(accessToken).getOrThrow()
            authRepository.saveRefreshToken(refreshToken).getOrThrow()
        }.recoverCatching { throwable ->
            // 부분 성공 시 롤백
            authRepository.clearAuthData().getOrThrow()
            throw throwable
        }
}
