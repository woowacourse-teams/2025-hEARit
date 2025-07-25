package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.BookmarkService
import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse
import com.onair.hearit.di.TokenProvider
import com.onair.hearit.domain.NoBookmarkException

class BookmarkRemoteDataSourceImpl(
    private val bookmarkService: BookmarkService,
) : BookmarkRemoteDataSource {
    override suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<BookmarkResponse> =
        runCatching {
            val response = bookmarkService.getBookmarks(getAuthHeader(), page, size)
            when (response.code()) {
                200 ->
                    response.body() ?: throw IllegalStateException(
                        ERROR_RESPONSE_BODY_NULL_MESSAGE,
                    )

                401 -> throw NoBookmarkException(ERROR_BOOKMARK_LOAD_MESSAGE)
                else -> throw Exception("API 호출 실패: ${response.code()} ${response.message()}")
            }
        }

    override suspend fun addBookmark(hearitId: Long): Result<BookmarkIdResponse> =
        handleApiCall(
            errorMessage = ERROR_BOOKMARK_ADD_MESSAGE,
            apiCall = { bookmarkService.postBookmark(getAuthHeader(), hearitId) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun deleteBookmark(bookmarkId: Long): Result<Unit> =
        handleApiCall(
            errorMessage = ERROR_BOOKMARK_DELETE_MESSAGE,
            apiCall = { bookmarkService.deleteBookmark(getAuthHeader(), bookmarkId) },
            transform = { },
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
        private const val ERROR_BOOKMARK_LOAD_MESSAGE = "북마크 조회 실패"
        private const val ERROR_BOOKMARK_ADD_MESSAGE = "북마크 생성 실패"
        private const val ERROR_BOOKMARK_DELETE_MESSAGE = "북마크 삭제 실패"
        private const val ERROR_RESPONSE_BODY_NULL_MESSAGE = "응답 바디가 null입니다."
        private const val TOKEN = "Bearer %s"
    }
}
