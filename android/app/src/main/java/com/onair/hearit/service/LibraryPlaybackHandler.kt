package com.onair.hearit.service

import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.usecase.GetBookmarksUseCase
import com.onair.hearit.service.model.LibraryLoadResult
import com.onair.hearit.service.model.LibraryPlayParams
import com.onair.hearit.service.model.PrefetchResult
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 라이브러리(북마크 목록) 기반 재생을 위한 핸들러.
 *
 * - 페이징된 북마크를 로드하여 [PlaybackInfo] 리스트로 만든다.
 * - 특정 "시드(seed)" 아이템(북마크 ID 또는 hearit ID) 기준으로 시작 인덱스/재생 위치를 계산한다.
 * - Media3 재생용 [MediaSession.MediaItemsWithStartPosition]으로 변환한다.
 * - 다음 페이지를 미리 불러(prefetch) 재생 중 끊김을 줄인다.
 */
@UnstableApi
@ServiceScoped
class LibraryPlaybackHandler @Inject constructor(
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
                playbackMode = "LIBRARY",
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

    /**
     * 다음 페이지를 미리 가져온다 (prefetch).
     * - 현재 [nextPage]가 null이면 더 이상 가져올 페이지 없음 → 빈 결과 반환
     * - 성공 시: MediaItem 리스트와 다음 nextPage를 함께 반환
     * - 실패/마지막 페이지: nextPage를 null로 세팅
     */
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
                createPlaybackInfos(pageResult.items).map { playbackInfo ->
                    mediaItemManager.buildMediaItem(
                        info = playbackInfo,
                        playbackMode = "LIBRARY",
                        bookmarkId = playbackInfo.bookmarkId,
                    )
                }
            PrefetchResult(newItems, nextPage)
        }

    /**
     * 단일 페이지를 로드하여 [PlaybackInfo] 리스트로 변환한다.
     * - 성공 시: 리스트 반환 및 [nextPage] 갱신
     * - 실패 시: 빈 리스트 반환 및 [nextPage]를 null로 설정
     */
    private suspend fun loadPage(page: Int): List<PlaybackInfo> {
        val pageResult =
            getBookmarksUseCase(page = page, size = DEFAULT_PAGE_SIZE)
                .getOrNull() ?: return emptyList()
        nextPage = if (!pageResult.paging.isLast) page + 1 else null
        return createPlaybackInfos(pageResult.items)
    }

    /**
     * "시드"가 현재 페이지에 없을 수 있으므로, 페이지를 순회하며 시드를 찾는다.
     * - 각 페이지에서 [PlaybackInfo]로 매핑 후, [findSeedInBookmarks]로 시드 인덱스를 검사
     * - 찾으면 지금까지 누적된 아이템 수를 기반으로 전역 인덱스 계산
     * - 끝까지 못 찾으면 seedIndex=-1로 반환
     */
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
                nextPage = if (!result.paging.isLast) (result.paging.page + 1) else null
                val globalIndex = allItems.size - items.size + seedInPage
                return LibraryLoadResult(allItems, globalIndex)
            }

            if (result.paging.isLast) {
                nextPage = null
                return LibraryLoadResult(allItems, -1)
            }
            currentPage++
        }
    }

    /**
     * 현재 페이지(혹은 주어진 리스트)에서 시드(북마크 ID 또는 hearit ID)와 매칭되는 인덱스를 찾는다.
     * - 없으면 -1
     */
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
            val position = bookmark.lastPlayTime ?: 0L

            PlaybackInfo(
                hearitId = bookmark.hearitId,
                audioUrl = audioUrl,
                title = bookmark.title,
                source = bookmark.sources.first().name,
                lastPosition = if (bookmark.playTime * 1000 - position < 5_000) 0L else position,
                bookmarkId = bookmark.bookmarkId,
            )
        }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 10
    }
}
