package com.onair.hearit.data.datasource

import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErrorResponseHandler {
    fun getError(exception: Throwable): NetworkResult.Failure =
        when (exception) {
            is HttpException -> {
                handleHttpException(exception)
            }

            is IOException -> {
                NetworkResult.Failure.NetworkConnection
            }

            else -> {
                NetworkResult.Failure.Unknown
            }
        }

    private fun handleHttpException(exception: HttpException): NetworkResult.Failure =
        when (exception.code()) {
            401 -> {
                NetworkResult.Failure.UnAuthorized
            }

            in 400..499 -> {
                NetworkResult.Failure.BadRequest(
                    code = exception.code(),
                    message = extractErrorMessage(exception.response()),
                )
            }

            in 500..599 -> {
                NetworkResult.Failure.InternalServer
            }

            else -> {
                NetworkResult.Failure.Unknown
            }
        }

    private fun extractErrorMessage(response: Response<*>?): String = response?.errorBody()?.string() ?: response?.message() ?: "알 수 없는 에러"
}
