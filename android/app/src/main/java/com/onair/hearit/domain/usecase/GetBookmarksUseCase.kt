package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.MediaFileRepository

class GetBookmarksUseCase(
    private val bookmarkRepository: BookmarkRepository,
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(
        page: Int?,
        size: Int?,
    ): Result<PageResult<Bookmark>> =
        runCatching {
            val bookmarks = bookmarkRepository.getBookmarks(page, size).getOrThrow()

            val updatedItems =
                bookmarks.items.map { bookmark ->
                    val audioUrl =
                        mediaFileRepository
                            .getOriginalAudioUrl(bookmark.hearitId)
                            .getOrThrow()
                            .url
                    bookmark.copy(audioUrl = audioUrl)
                }

            PageResult(
                items = updatedItems,
                paging = bookmarks.paging,
            )
        }
}
