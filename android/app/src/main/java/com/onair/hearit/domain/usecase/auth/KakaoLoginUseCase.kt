package com.onair.hearit.domain.usecase.auth

import com.onair.hearit.domain.model.LoginToken
import com.onair.hearit.domain.repository.AuthRepository

class KakaoLoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(accessToken: String): Result<LoginToken> = authRepository.kakaoLogin(accessToken)
}
