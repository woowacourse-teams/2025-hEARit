package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.BookmarkService
import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse
import com.onair.hearit.di.TokenProvider

class BookmarkRemoteDataSourceImpl(
    private val bookmarkService: BookmarkService,
) : BookmarkRemoteDataSource {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<BookmarkResponse> =
        handleApiCall(
            errorMessage = ERROR_BOOKMARK_LOAD_MESSAGE,
            apiCall = { bookmarkService.getBookmarks(getAuthHeader(), page, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun addBookmark(hearitId: Long): Result<BookmarkIdResponse> =
        handleApiCall(
            errorMessage = ERROR_BOOKMARK_ADD_MESSAGE,
            apiCall = { bookmarkService.postBookmark(getAuthHeader(), hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun deleteBookmark(
        hearitId: Long,
        bookmarkId: Long,
    ): Result<Unit> =
        handleApiCall(
            errorMessage = ERROR_BOOKMARK_DELETE_MESSAGE,
            apiCall = { bookmarkService.deleteBookmark(getAuthHeader(), hearitId, bookmarkId) },
            transform = { response ->
                if (response.isSuccessful) {
                    Unit
                } else {
                    throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
                }
            },
        )

    private fun getAuthHeader(): String {
        val token = requireNotNull(TokenProvider.accessToken) { ERROR_TOKEN_NOT_FOUND_MESSAGE }
        return TOKEN.format(token)
    }

    companion object {
        private const val ERROR_BOOKMARK_LOAD_MESSAGE = "북마크 조회 실패"
        private const val ERROR_BOOKMARK_ADD_MESSAGE = "북마크 생성 실패"
        private const val ERROR_BOOKMARK_DELETE_MESSAGE = "북마크 삭제 실패"
        private const val ERROR_RESPONSE_BODY_NULL_MESSAGE = "응답 바디가 null입니다."
        private const val ERROR_TOKEN_NOT_FOUND_MESSAGE = "토큰이 존재하지 않음"
        private const val TOKEN = "Bearer %s"
    }
}
