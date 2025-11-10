package com.onair.hearit.domain.exception

sealed class AuthException(
    message: String,
) : Exception(message) {
    object KakaoLogoutFailed : AuthException("카카오 로그아웃 실패")

    object ClearTokenFailed : AuthException("액세스 토큰 삭제 실패")

    object ClearUserDataFailed : AuthException("유저 정보 삭제 실패")
}
