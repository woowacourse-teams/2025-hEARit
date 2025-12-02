package com.onair.hearit.domain

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.domain.exception.DomainException

object DomainExceptionMapper {
    fun toDomainException(failure: NetworkResult.Failure): Throwable =
        when (failure) {
            is NetworkResult.Failure.BadRequest ->
                IllegalStateException("$ERROR_BAD_REQUEST_MESSAGE ${failure.code} - ${failure.message}")

            is NetworkResult.Failure.InternalServer ->
                IllegalStateException(ERROR_SERVER_MESSAGE)

            is NetworkResult.Failure.Unknown ->
                IllegalStateException(ERROR_NETWORK_MESSAGE)

            NetworkResult.Failure.NetworkConnection ->
                DomainException.NetworkConnection

            NetworkResult.Failure.UnAuthorized ->
                DomainException.UserNotRegistered
        }

    private const val ERROR_NETWORK_MESSAGE = "알 수 없는 네트워크 오류"
    private const val ERROR_SERVER_MESSAGE = "서버 오류"
    private const val ERROR_BAD_REQUEST_MESSAGE = "잘못된 요청:"
}
