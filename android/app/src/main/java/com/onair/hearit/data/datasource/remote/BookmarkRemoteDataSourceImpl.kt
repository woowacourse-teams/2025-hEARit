package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.BookmarkService
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.datasource.handleApiCallUnit
import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse
import javax.inject.Inject

class BookmarkRemoteDataSourceImpl @Inject constructor(
    private val bookmarkService: BookmarkService,
    private val errorResponseHandler: ErrorResponseHandler,
) : BookmarkRemoteDataSource {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
        filter: String,
        sort: String?,
    ): NetworkResult<BookmarkResponse> =
        handleApiCall(
            apiCall = { bookmarkService.getBookmarks(page, size, filter) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun addBookmark(hearitId: Long): NetworkResult<BookmarkIdResponse> =
        handleApiCall(
            apiCall = { bookmarkService.postBookmark(hearitId) },
            errorHandler = errorResponseHandler,
        )

    override suspend fun deleteBookmark(bookmarkId: Long): NetworkResult<Unit> =
        handleApiCallUnit(
            apiCall = { bookmarkService.deleteBookmark(bookmarkId) },
            errorHandler = errorResponseHandler,
        )
}
