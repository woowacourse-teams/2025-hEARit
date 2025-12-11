package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(
        page: Int? = null,
        size: Int? = null,
        filter: String = "all",
    ): Result<PageResult<Bookmark>> =
        runCatching {
            val bookmarksPage = bookmarkRepository.getBookmarks(page, size, filter).getOrThrow()

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
