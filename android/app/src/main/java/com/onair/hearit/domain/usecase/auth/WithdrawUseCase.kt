package com.onair.hearit.domain.usecase.auth

import com.kakao.sdk.user.UserApiClient
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.UserRepository
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WithdrawUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val kakaoClient: UserApiClient = UserApiClient.instance,
) {
    suspend operator fun invoke(): Result<Unit> =
        runCatching {
            suspendCoroutine { cont ->
                kakaoClient.unlink { error ->
                    if (error != null) {
                        cont.resumeWithException(error)
                    } else {
                        cont.resume(Unit)
                    }
                }
            }

            authRepository.withdraw().getOrThrow()

            authRepository.clearAuthData().getOrThrow()
            userRepository.clearUserData().getOrThrow()

            Unit
        }
}
