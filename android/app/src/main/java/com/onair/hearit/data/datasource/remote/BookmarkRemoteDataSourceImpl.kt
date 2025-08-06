package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.BookmarkService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse

class BookmarkRemoteDataSourceImpl(
    private val bookmarkService: BookmarkService,
    private val errorResponseHandler: ErrorResponseHandler,
) : BookmarkRemoteDataSource {
    override suspend fun getBookmarks(
        token: String?,
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<BookmarkResponse>> =
        handleApiCall(
            apiCall = { bookmarkService.getBookmarks(token, page, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun addBookmark(
        token: String?,
        hearitId: Long,
    ): Result<NetworkResult<BookmarkIdResponse>> =
        handleApiCall(
            apiCall = { bookmarkService.postBookmark(token, hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )

    override suspend fun deleteBookmark(
        token: String?,
        bookmarkId: Long,
    ): Result<NetworkResult<Unit>> =
        handleApiCall(
            apiCall = { bookmarkService.deleteBookmark(token, bookmarkId) },
            transform = { },
            errorHandler = errorResponseHandler,
        )
}
