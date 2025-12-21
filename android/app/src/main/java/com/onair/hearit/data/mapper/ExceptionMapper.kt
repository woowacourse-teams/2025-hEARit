package com.onair.hearit.data.mapper

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.domain.exception.DomainException

object ExceptionMapper {
    fun mapToThrowable(failure: NetworkResult.Failure): Throwable =
        when (failure) {
            // 사용자 액션 불필요 → IllegalStateException (시스템 오류)
            is NetworkResult.Failure.BadRequest -> {
                IllegalStateException("$ERROR_BAD_REQUEST_MESSAGE ${failure.code} - ${failure.message}")
            }

            is NetworkResult.Failure.InternalServer -> {
                IllegalStateException(ERROR_SERVER_MESSAGE)
            }

            is NetworkResult.Failure.Unknown -> {
                IllegalStateException(ERROR_UNKNOWN_MESSAGE)
            }

            // 사용자 액션 필요 → DomainException
            NetworkResult.Failure.NetworkConnection -> {
                DomainException.NetworkConnection
            }

            NetworkResult.Failure.UnAuthorized -> {
                DomainException.UserNotRegistered
            }
        }
}

private const val ERROR_UNKNOWN_MESSAGE = "알 수 없는 오류가 발생했습니다"
private const val ERROR_SERVER_MESSAGE = "서버 오류"
private const val ERROR_BAD_REQUEST_MESSAGE = "잘못된 요청:"
