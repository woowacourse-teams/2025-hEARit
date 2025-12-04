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
            // body가 null이면 Unit으로 처리 (Response<Unit>인 경우 대응)
            @Suppress("UNCHECKED_CAST")
            NetworkResult.Success(response.body() ?: Unit as T)
        } else {
            errorHandler.getError(HttpException(response))
        }
    } catch (e: Exception) {
        errorHandler.getError(e)
    }
