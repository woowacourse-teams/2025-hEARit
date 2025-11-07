package com.onair.hearit.domain.usecase.auth

import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.UserRepository

class WithdrawUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): Result<Boolean> =
        runCatching {
            authRepository.withdraw().getOrThrow()
            authRepository.clearAuthData().getOrThrow()
            userRepository.clearUserData().getOrThrow()
            true
        }
}
