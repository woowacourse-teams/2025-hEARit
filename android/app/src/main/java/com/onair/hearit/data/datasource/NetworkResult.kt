package com.onair.hearit.data.datasource

sealed class NetworkResult<out T> {
    data class Success<T>(
        val data: T,
    ) : NetworkResult<T>()

    sealed class Failure : NetworkResult<Nothing>() {
        data object Unknown : Failure()

        data object InternalServer : Failure()

        data object UnAuthorized : Failure()

        class BadRequest(
            val code: Int,
            val message: String,
        ) : Failure()
    }
}
