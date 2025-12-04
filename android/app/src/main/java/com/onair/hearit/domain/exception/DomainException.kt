package com.onair.hearit.domain.exception

sealed class DomainException(
    message: String,
) : Exception(message) {
    data object NetworkConnection : DomainException(ERROR_CHECK_NETWORK_MESSAGE)

    data object UserNotRegistered : DomainException(ERROR_UNAUTHORIZED_MESSAGE)
}

private const val ERROR_CHECK_NETWORK_MESSAGE = "네트워크 연결을 확인해주세요"
private const val ERROR_UNAUTHORIZED_MESSAGE = "로그인이 필요합니다"
