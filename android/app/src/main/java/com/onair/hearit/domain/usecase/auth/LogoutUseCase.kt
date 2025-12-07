package com.onair.hearit.domain.usecase.auth

import com.kakao.sdk.user.UserApiClient
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) {
    private val kakaoClient: UserApiClient
        get() = UserApiClient.instance

    suspend operator fun invoke(): Result<Unit> =
        runCatching {
            runCatching {
                suspendCoroutine { cont ->
                    kakaoClient.logout { error ->
                        if (error != null) {
                            Timber.e(error, "카카오 로그아웃 실패")
                            cont.resumeWithException(error)
                        } else {
                            Timber.d("카카오 로그아웃 성공")
                            cont.resume(Unit)
                        }
                    }
                }
            }.onFailure {
                Timber.w(it, "카카오 로그아웃 실패했지만 로컬 데이터는 정리합니다")
            }

            authRepository
                .clearAuthData()
                .onFailure { Timber.e(it, "토큰 삭제 실패") }
                .getOrThrow()

            userRepository
                .clearUserData()
                .onFailure { Timber.e(it, "유저 정보 삭제 실패") }
                .getOrThrow()

            Unit
        }
}
