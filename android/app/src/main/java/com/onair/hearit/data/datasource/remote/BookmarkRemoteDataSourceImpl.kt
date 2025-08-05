package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.BookmarkService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse
import com.onair.hearit.di.TokenProvider

class BookmarkRemoteDataSourceImpl(
    private val bookmarkService: BookmarkService,
    private val errorResponseHandler: ErrorResponseHandler,
) : BookmarkRemoteDataSource {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<BookmarkResponse>> =
        handleApiCall(
            apiCall = { bookmarkService.getBookmarks(getAuthHeader(), page, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun addBookmark(hearitId: Long): Result<NetworkResult<BookmarkIdResponse>> =
        handleApiCall(
            apiCall = { bookmarkService.postBookmark(getAuthHeader(), hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun deleteBookmark(bookmarkId: Long): Result<NetworkResult<Unit>> =
        handleApiCall(
            apiCall = { bookmarkService.deleteBookmark(getAuthHeader(), bookmarkId) },
            transform = { },
            errorHandler = errorResponseHandler,
        )

    private fun getAuthHeader(): String? {
        val token = TokenProvider.accessToken
        return if (token.isNullOrBlank()) {
            null
        } else {
            TOKEN.format(token)
        }
    }

    companion object {
        private const val TOKEN = "Bearer %s"
    }
}
