package com.onair.hearit.data

import retrofit2.Response

inline fun <T, R> handleApiCall(
    errorMessage: String,
    apiCall: () -> Response<T>,
    transform: (Response<T>) -> R,
): Result<R> =
    runCatching {
        val response = apiCall()
        if (!response.isSuccessful) {
            throw Exception("API 호출 실패: ${response.code()} ${response.message()}")
        }
        transform(response)
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(Exception("$errorMessage\n인터넷 연결을 확인해주세요.", it)) },
    )
