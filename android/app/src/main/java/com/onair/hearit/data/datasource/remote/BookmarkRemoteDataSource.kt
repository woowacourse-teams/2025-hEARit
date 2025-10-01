package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.BookmarkIdResponse
import com.onair.hearit.data.dto.BookmarkResponse
import com.onair.hearit.data.dto.PlayingBookmarkResponse

interface BookmarkRemoteDataSource {
    suspend fun getBookmarks(
        page: Int?,
        size: Int?,
    ): Result<NetworkResult<BookmarkResponse>>

    suspend fun getPlayingBookmarkHearits(): Result<NetworkResult<List<PlayingBookmarkResponse>>>

    suspend fun addBookmark(hearitId: Long): Result<NetworkResult<BookmarkIdResponse>>

    suspend fun deleteBookmark(bookmarkId: Long): Result<NetworkResult<Unit>>
}
