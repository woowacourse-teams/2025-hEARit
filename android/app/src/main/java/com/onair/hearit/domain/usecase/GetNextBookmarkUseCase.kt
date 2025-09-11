package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.MediaFileRepository

class GetNextBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository,
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(currentId: Long): Result<Bookmark?> =
        runCatching {
            val nextBookmark = bookmarkRepository.getNextBookmark(currentId).getOrThrow()

            nextBookmark?.let { bookmark ->
                val audioUrl =
                    mediaFileRepository
                        .getOriginalAudioUrl(bookmark.hearitId)
                        .getOrThrow()
                        .url

                bookmark.copy(audioUrl = audioUrl)
            }
        }
}
