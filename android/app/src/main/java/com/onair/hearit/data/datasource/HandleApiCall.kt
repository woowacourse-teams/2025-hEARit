package com.onair.hearit.data.datasource

import retrofit2.HttpException
import retrofit2.Response

suspend fun <T> handleApiCall(
    apiCall: suspend () -> Response<T>,
    errorHandler: ErrorResponseHandler,
): NetworkResult<T> =
    try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let { body ->
                NetworkResult.Success(body)
            } ?: NetworkResult.Failure.Unknown
        } else {
            errorHandler.getError(HttpException(response))
        }
    } catch (e: Exception) {
        errorHandler.getError(e)
    }
