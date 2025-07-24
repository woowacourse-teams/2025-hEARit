package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.BookmarkService
import com.onair.hearit.data.dto.BookmarkResponse

class BookmarkRemoteDataSourceImpl(
    private val bookmarkService: BookmarkService,
) : BookmarkRemoteDataSource {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<BookmarkResponse> =
        handleApiCall(
            errorMessage = ERROR_BOOKMARK_LOAD_MESSAGE,
            apiCall = { bookmarkService.getBookmarks(page, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun addBookmark(hearitId: Long): Result<Unit> =
        handleApiCall(
            errorMessage = ERROR_BOOKMARK_ADD_MESSAGE,
            apiCall = { bookmarkService.postBookmark(hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    companion object {
        private const val ERROR_BOOKMARK_LOAD_MESSAGE = "북마크 조회 실패"
        private const val ERROR_BOOKMARK_ADD_MESSAGE = "북마크 생성 실패"
        private const val ERROR_RESPONSE_BODY_NULL_MESSAGE = "응답 바디가 null입니다."
    }
}
