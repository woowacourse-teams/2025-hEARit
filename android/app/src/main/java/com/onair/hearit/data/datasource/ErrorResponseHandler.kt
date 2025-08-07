package com.onair.hearit.data.datasource

import retrofit2.HttpException
import retrofit2.Response

class ErrorResponseHandler {
    fun getError(exception: Throwable): NetworkResult.Failure =
        when (exception) {
            is HttpException -> {
                when (exception.code()) {
                    401 -> NetworkResult.Failure.UnAuthorized
                    in 500..599 -> NetworkResult.Failure.InternalServer
                    in 400..499 -> {
                        val code = exception.code()
                        val message = extractErrorMessage(exception.response())
                        NetworkResult.Failure.BadRequest(code, message)
                    }

                    else -> NetworkResult.Failure.Unknown
                }
            }

            else -> NetworkResult.Failure.Unknown
        }

    private fun extractErrorMessage(response: Response<*>?): String = response?.message().orEmpty()
}
