package com.onair.hearit.service

import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.usecase.GetBookmarksUseCase
import com.onair.hearit.service.model.LibraryLoadResult
import com.onair.hearit.service.model.LibraryPlayParams
import com.onair.hearit.service.model.PrefetchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@UnstableApi
class LibraryPlaybackHandler(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val mediaItemManager: PlaybackMediaItemManager,
) {
    private var nextPage: Int? = null

    suspend fun loadLibraryItemsWithStartPosition(params: LibraryPlayParams): MediaSession.MediaItemsWithStartPosition =
        withContext(Dispatchers.IO) {
            val loadResult = loadLibraryItemsWithIndex(params)

            val seedIndex =
                loadResult.seedIndex.coerceIn(
                    0,
                    (loadResult.items.size - 1).coerceAtLeast(0),
                )
            val seedItemLast = loadResult.items.getOrNull(seedIndex)?.lastPosition ?: 0L

            val startPositionMs =
                when {
                    params.startPositionMs > 0L -> params.startPositionMs
                    else -> seedItemLast
                }

            mediaItemManager.toItemsWithStart(
                items = loadResult.items,
                startIndex = seedIndex,
                startPositionMs = startPositionMs,
            )
        }

    /** 시드 아이템의 전역 인덱스와 전체 목록 구성 */
    suspend fun loadLibraryItemsWithIndex(params: LibraryPlayParams): LibraryLoadResult =
        withContext(Dispatchers.IO) {
            if (params.seedBookmarkId <= 0 && params.seedHearitId <= 0) {
                LibraryLoadResult(loadPage(0), 0)
            } else {
                findSeedAcrossPages(params)
            }
        }

    /** 다음 페이지 미리 가져오기 */
    suspend fun prefetchNextPage(): PrefetchResult =
        withContext(Dispatchers.IO) {
            val pageToLoad = nextPage ?: return@withContext PrefetchResult(emptyList(), null)
            val pageResult =
                getBookmarksUseCase(page = pageToLoad, size = DEFAULT_PAGE_SIZE).getOrNull()

            if (pageResult == null) {
                nextPage = null
                return@withContext PrefetchResult(emptyList(), null)
            }

            nextPage = if (!pageResult.paging.isLast) pageResult.paging.page + 1 else null
            val newItems =
                createPlaybackInfos(pageResult.items).map { mediaItemManager.buildMediaItem(it) }
            PrefetchResult(newItems, nextPage)
        }

    private suspend fun loadPage(page: Int): List<PlaybackInfo> {
        val pageResult =
            getBookmarksUseCase(page = page, size = DEFAULT_PAGE_SIZE)
                .getOrNull() ?: return emptyList()
        nextPage = if (!pageResult.paging.isLast) page + 1 else null
        return createPlaybackInfos(pageResult.items)
    }

    private suspend fun findSeedAcrossPages(params: LibraryPlayParams): LibraryLoadResult {
        val allItems = mutableListOf<PlaybackInfo>()
        var currentPage = 0
        while (true) {
            val result =
                getBookmarksUseCase(page = currentPage, size = DEFAULT_PAGE_SIZE).getOrNull()
                    ?: return LibraryLoadResult(allItems, -1)

            val items = createPlaybackInfos(result.items)
            val seedInPage = findSeedInBookmarks(items, params)

            allItems.addAll(items)
            if (seedInPage >= 0) {
                val globalIndex = allItems.size - items.size + seedInPage
                return LibraryLoadResult(allItems, globalIndex)
            }

            if (result.paging.isLast) return LibraryLoadResult(allItems, -1)
            currentPage++
        }
    }

    private fun findSeedInBookmarks(
        playbackInfos: List<PlaybackInfo>,
        params: LibraryPlayParams,
    ): Int =
        playbackInfos.indexOfFirst { info ->
            (params.seedBookmarkId > 0 && info.bookmarkId == params.seedBookmarkId) ||
                (params.seedHearitId > 0 && info.hearitId == params.seedHearitId)
        }

    /** API 북마크 → PlaybackInfo로 매핑 (audioUrl 누락 시 제외) */
    fun createPlaybackInfos(bookmarks: List<Bookmark>): List<PlaybackInfo> =
        bookmarks.mapNotNull { bookmark ->
            val audioUrl = bookmark.audioUrl ?: return@mapNotNull null
            PlaybackInfo(
                hearitId = bookmark.hearitId,
                audioUrl = audioUrl,
                title = bookmark.title,
                source = "hEARit",
                lastPosition = bookmark.lastPlayTime,
                bookmarkId = bookmark.bookmarkId,
            )
        }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 10
    }
}
