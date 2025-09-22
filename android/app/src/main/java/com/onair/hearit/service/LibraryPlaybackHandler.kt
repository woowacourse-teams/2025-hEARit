package com.onair.hearit.service

import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.usecase.GetBookmarksUseCase
import com.onair.hearit.service.model.LibraryLoadResult
import com.onair.hearit.service.model.LibraryPlayParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@UnstableApi
class LibraryPlaybackHandler(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val mediaItemManager: PlaybackMediaItemManager,
) {
    private var nextPage: Int? = null

    suspend fun loadLibraryItemsWithIndex(params: LibraryPlayParams): LibraryLoadResult =
        withContext(Dispatchers.IO) {
            if (params.seedBookmarkId <= 0 && params.seedHearitId <= 0) {
                val items = loadSinglePage(0)
                return@withContext LibraryLoadResult(items, 0)
            }
            loadUntilSeedFoundWithIndex(params)
        }

    suspend fun prefetchNextPage(): Pair<List<MediaItem>, Int?> =
        withContext(Dispatchers.IO) {
            val pageToLoad = nextPage ?: return@withContext Pair(emptyList(), null)

            val pageResult =
                getBookmarksUseCase(page = pageToLoad, size = DEFAULT_PAGE_SIZE)
                    .getOrNull() ?: return@withContext Pair(emptyList(), null)

            val newItems = createMediaItems(pageResult.items)
            nextPage = if (!pageResult.paging.isLast) pageResult.paging.page + 1 else null
            Pair(newItems, nextPage)
        }

    private suspend fun loadSinglePage(page: Int): List<MediaItem> =
        withContext(Dispatchers.IO) {
            val pageResult =
                getBookmarksUseCase(page = page, size = DEFAULT_PAGE_SIZE)
                    .getOrNull() ?: return@withContext emptyList()

            nextPage = if (!pageResult.paging.isLast) page + 1 else null
            createMediaItems(pageResult.items)
        }

    private suspend fun loadUntilSeedFoundWithIndex(params: LibraryPlayParams): LibraryLoadResult =
        withContext(Dispatchers.IO) {
            val allItems = mutableListOf<MediaItem>()
            var currentPage = 0

            do {
                val pageResult =
                    getBookmarksUseCase(page = currentPage, size = DEFAULT_PAGE_SIZE)
                        .getOrNull()

                if (pageResult == null) {
                    return@withContext LibraryLoadResult(allItems, -1)
                }

                val seedIndexInPage = findSeedInBookmarks(pageResult.items, params)
                if (seedIndexInPage >= 0) {
                    val pageItems = createMediaItems(pageResult.items)
                    allItems.addAll(pageItems)
                    val globalSeedIndex = allItems.size - pageItems.size + seedIndexInPage
                    return@withContext LibraryLoadResult(allItems, globalSeedIndex)
                }

                allItems.addAll(createMediaItems(pageResult.items))
                currentPage++
            } while (!pageResult.paging.isLast)

            LibraryLoadResult(allItems, -1)
        }

    private fun findSeedInBookmarks(
        bookmarks: List<Bookmark>,
        params: LibraryPlayParams,
    ): Int =
        bookmarks.indexOfFirst { bookmark ->
            (params.seedBookmarkId > 0 && bookmark.bookmarkId == params.seedBookmarkId) ||
                (params.seedHearitId > 0 && bookmark.hearitId == params.seedHearitId)
        }

    fun createMediaItems(bookmarks: List<Bookmark>): List<MediaItem> =
        bookmarks.mapNotNull { bookmark ->
            val audioUrl = bookmark.audioUrl
            if (audioUrl.isNullOrBlank()) {
                return@mapNotNull null
            }
            mediaItemManager.buildMediaItem(
                info =
                    PlaybackInfo(
                        hearitId = bookmark.hearitId,
                        audioUrl = audioUrl,
                        title = bookmark.title,
                        source = "hEARit",
                    ),
                playbackMode = "LIBRARY",
                bookmarkId = bookmark.bookmarkId,
            )
        }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 10
    }
}
