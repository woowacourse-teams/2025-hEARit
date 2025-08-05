package com.onair.hearit.data.datasource

import retrofit2.HttpException
import retrofit2.Response

inline fun <T, R> handleApiCall(
    apiCall: () -> Response<T>,
    transform: (Response<T>) -> R,
    errorHandler: ErrorResponseHandler,
): Result<NetworkResult<R>> =
    runCatching {
        val response = apiCall()
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        NetworkResult.Success(transform(response))
    }.recoverCatching {
        errorHandler.getError(it)
    }
