package com.onair.hearit.data.datasource

sealed interface NetworkResult<out T> {
    data class Success<T>(
        val data: T,
    ) : NetworkResult<T>

    sealed interface Failure : NetworkResult<Nothing> {
        data object Unknown : Failure

        data object InternalServer : Failure

        data object UnAuthorized : Failure

        data object NetworkConnection : Failure

        class BadRequest(
            val code: Int,
            val message: String,
        ) : Failure
    }
}
