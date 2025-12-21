package com.onair.hearit.data.datasource

import retrofit2.HttpException
import retrofit2.Response
import kotlin.coroutines.cancellation.CancellationException

/**
 * Response body가 있는 API 호출을 처리합니다.
 * @param T Response body 타입 (Non-null)
 */
suspend fun <T : Any> handleApiCall(
    apiCall: suspend () -> Response<T>,
    errorHandler: ErrorResponseHandler,
): NetworkResult<T> =
    runCatching { apiCall() }
        .fold(
            onSuccess = { response ->
                when {
                    response.isSuccessful -> {
                        response.body()?.let { NetworkResult.Success(it) }
                            ?: errorHandler.getError(
                                IllegalStateException("response body가 null입니다"),
                            )
                    }

                    else -> {
                        errorHandler.getError(HttpException(response))
                    }
                }
            },
            onFailure = { e ->
                if (e is CancellationException) throw e
                errorHandler.getError(e)
            },
        )

/**
 * Response body가 없는 API 호출을 처리합니다.
 */
suspend fun handleApiCallUnit(
    apiCall: suspend () -> Response<Unit>,
    errorHandler: ErrorResponseHandler,
): NetworkResult<Unit> =
    runCatching { apiCall() }
        .fold(
            onSuccess = { response ->
                when {
                    response.isSuccessful -> NetworkResult.Success(Unit)
                    else -> errorHandler.getError(HttpException(response))
                }
            },
            onFailure = { e ->
                if (e is CancellationException) throw e
                errorHandler.getError(e)
            },
        )
