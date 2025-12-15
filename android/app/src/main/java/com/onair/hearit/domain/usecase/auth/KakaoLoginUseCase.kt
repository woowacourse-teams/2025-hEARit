package com.onair.hearit.domain.usecase.auth

import com.onair.hearit.domain.model.LoginToken
import com.onair.hearit.domain.repository.AuthRepository
import javax.inject.Inject

class KakaoLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(accessToken: String): Result<LoginToken> = authRepository.kakaoLogin(accessToken)
}
