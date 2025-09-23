package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GetBookmarksUseCase(
    private val bookmarkRepository: BookmarkRepository,
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(
        page: Int? = null,
        size: Int? = null,
    ): Result<PageResult<Bookmark>> =
        runCatching {
            val bookmarksPage = bookmarkRepository.getBookmarks(page, size).getOrThrow()

            val updatedItems =
                coroutineScope {
                    bookmarksPage.items
                        .map { bookmark ->
                            async {
                                val audioUrl =
                                    mediaFileRepository
                                        .getOriginalAudioUrl(bookmark.hearitId)
                                        .getOrThrow()
                                        .url
                                bookmark.copy(audioUrl = audioUrl)
                            }
                        }.awaitAll()
                }

            PageResult(
                items = updatedItems,
                paging = bookmarksPage.paging,
            )
        }
}
