package com.onair.hearit.service

import android.os.Bundle
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.di.UseCaseProvider
import com.onair.hearit.domain.model.Bookmark
import com.onair.hearit.domain.model.PlaybackInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class PlaybackSessionCallback(
    private val serviceScope: CoroutineScope,
) : MediaSession.Callback {
    private val mediaItemHelper = PlaybackMediaItemManager()
    private var nextPage: Int? = null

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): MediaSession.ConnectionResult {
        val baseResult = super.onConnect(session, controller)

        val sessionCommands =
            baseResult.availableSessionCommands
                .buildUpon()
                .add(PRELOAD_RECENT_COMMAND)
                .add(START_LIBRARY_PLAY_COMMAND)
                .add(PREFETCH_NEXT_COMMAND)
                .build()

        return MediaSession.ConnectionResult
            .AcceptedResultBuilder(session)
            .setAvailableSessionCommands(sessionCommands)
            .setAvailablePlayerCommands(baseResult.availablePlayerCommands)
            .build()
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
        executeAsync("onSetMediaItems") {
            preRecordCurrent(mediaSession)
            processMediaItems(mediaItems, startIndex, startPositionMs)
        }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        command: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> =
        executeAsync("onCustomCommand") {
            when (command.customAction) {
                ACTION_START_LIBRARY_PLAY -> handleStartLibraryPlay(session, args)
                ACTION_PREFETCH_NEXT -> handlePrefetchNext(session)
                ACTION_PRELOAD_RECENT -> handlePreloadRecent(session)
                else -> super.onCustomCommand(session, controller, command, args).get()
            }
        }

    override fun onPlaybackResumption(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
        executeAsync("onPlaybackResumption") {
            val recentInfo = loadRecentInfo()
            recentInfo?.let { mediaItemHelper.toItemsWithStart(it) }
                ?: EMPTY_MEDIA_ITEMS_WITH_START
        }

    override fun onAddMediaItems(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
    ): ListenableFuture<List<MediaItem>> =
        executeAsync("onAddMediaItems") {
            mediaItems
                .map { item ->
                    runCatching { resolveMediaItem(item) }.getOrElse { item }
                }.ifEmpty { mediaItems }
        }

    private suspend fun processMediaItems(
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): MediaSession.MediaItemsWithStartPosition {
        val resolvedItems =
            mediaItems
                .map { item ->
                    runCatching { resolveMediaItem(item) }.getOrElse { item }
                }.ifEmpty { mediaItems }

        val safeStartIndex =
            if (resolvedItems.isNotEmpty()) {
                startIndex.coerceIn(0, resolvedItems.size - 1)
            } else {
                0
            }

        val startPosition = calculateStartPosition(resolvedItems, safeStartIndex, startPositionMs)

        return MediaSession.MediaItemsWithStartPosition(
            resolvedItems,
            safeStartIndex,
            startPosition,
        )
    }

    private fun calculateStartPosition(
        items: List<MediaItem>,
        startIndex: Int,
        requestedPosition: Long,
    ): Long {
        if (requestedPosition > 0) return requestedPosition

        val extraPosition =
            items
                .getOrNull(startIndex)
                ?.mediaMetadata
                ?.extras
                ?.getLong(EXTRA_START_POSITION, -1L) ?: -1L

        return if (extraPosition > 0) extraPosition else 0L
    }

    private suspend fun handleStartLibraryPlay(
        session: MediaSession,
        args: Bundle,
    ): SessionResult {
        val playParams = LibraryPlayParams.fromBundle(args)
        val loadResult = loadLibraryItemsWithIndex(playParams)

        if (loadResult.items.isEmpty()) {
            return SessionResult(SessionError.ERROR_BAD_VALUE)
        }
        preRecordCurrent(session)

        withContext(Dispatchers.Main) {
            session.player.setMediaItems(
                loadResult.items,
                loadResult.seedIndex,
                playParams.startPosition,
            )
            session.player.prepare()
            session.player.play()
        }

        return SessionResult(SessionResult.RESULT_SUCCESS)
    }

    private suspend fun loadLibraryItemsWithIndex(params: LibraryPlayParams): LibraryLoadResult {
        // 시드가 없으면 첫 페이지만 로드
        if (params.seedBookmarkId <= 0 && params.seedHearitId <= 0) {
            val items = loadSinglePage(0)
            return LibraryLoadResult(items, 0)
        }

        // 시드가 있으면 찾으면서 인덱스도 계산
        return loadUntilSeedFoundWithIndex(params)
    }

    private suspend fun loadSinglePage(page: Int): List<MediaItem> {
        val pageResult =
            UseCaseProvider
                .getBookmarksUseCase(page = page, size = DEFAULT_PAGE_SIZE)
                .getOrNull() ?: return emptyList()

        nextPage = if (!pageResult.paging.isLast) page + 1 else null
        return createMediaItems(pageResult.items)
    }

    private suspend fun loadUntilSeedFoundWithIndex(params: LibraryPlayParams): LibraryLoadResult {
        val allItems = mutableListOf<MediaItem>()
        var currentPage = 0
        var globalSeedIndex = -1

        do {
            val pageResult =
                UseCaseProvider
                    .getBookmarksUseCase(page = currentPage, size = DEFAULT_PAGE_SIZE)
                    .getOrNull() ?: break

            // Bookmark 레벨에서 시드 위치 찾기 (효율적)
            val seedIndexInPage = findSeedInBookmarks(pageResult.items, params)
            if (seedIndexInPage >= 0 && globalSeedIndex < 0) {
                globalSeedIndex = allItems.size + seedIndexInPage
            }

            val pageItems = createMediaItems(pageResult.items)
            allItems.addAll(pageItems)
            nextPage = if (!pageResult.paging.isLast) pageResult.paging.page + 1 else null

            if (globalSeedIndex >= 0 || pageResult.paging.isLast) break
            currentPage++
        } while (true)

        return LibraryLoadResult(allItems, globalSeedIndex.coerceAtLeast(0))
    }

    private fun findSeedInBookmarks(
        bookmarks: List<Bookmark>,
        params: LibraryPlayParams,
    ): Int {
        // bookmarkId 우선 검색
        if (params.seedBookmarkId > 0) {
            bookmarks.forEachIndexed { index, bookmark ->
                if (bookmark.bookmarkId == params.seedBookmarkId) return index
            }
        }

        // hearitId로 검색
        if (params.seedHearitId > 0) {
            bookmarks.forEachIndexed { index, bookmark ->
                if (bookmark.hearitId == params.seedHearitId) return index
            }
        }

        return -1
    }

    private fun createMediaItems(bookmarks: List<Bookmark>): List<MediaItem> =
        bookmarks.map { bookmark ->
            mediaItemHelper.buildMediaItem(
                info =
                    PlaybackInfo(
                        hearitId = bookmark.hearitId,
                        audioUrl = checkNotNull(bookmark.audioUrl),
                        title = bookmark.title,
                        source = "hEARit",
                    ),
                playbackMode = "LIBRARY",
                bookmarkId = bookmark.bookmarkId,
            )
        }

    private suspend fun handlePrefetchNext(session: MediaSession): SessionResult {
        val pageToLoad = nextPage ?: return SessionResult(SessionResult.RESULT_SUCCESS)

        val pageResult =
            UseCaseProvider
                .getBookmarksUseCase(page = pageToLoad, size = DEFAULT_PAGE_SIZE)
                .getOrNull() ?: return SessionResult(SessionError.ERROR_IO)

        val newItems = createMediaItems(pageResult.items)
        nextPage = if (!pageResult.paging.isLast) pageResult.paging.page + 1 else null

        withContext(Dispatchers.Main) {
            session.player.addMediaItems(newItems)
        }

        return SessionResult(SessionResult.RESULT_SUCCESS)
    }

    private suspend fun handlePreloadRecent(session: MediaSession): SessionResult {
        val recentInfo = loadRecentInfo()
        recentInfo?.let { prepareRecentItem(session, it) }
        return SessionResult(SessionResult.RESULT_SUCCESS)
    }

    private suspend fun loadRecentInfo(): PlaybackInfo? =
        withContext(Dispatchers.IO) {
            RepositoryProvider.recentHearitRepository
                .getRecentHearit()
                .getOrNull()
                ?.let { recent ->
                    UseCaseProvider.getPlaybackInfoUseCase(recent.id).getOrNull()
                }
        }

    private fun prepareRecentItem(
        session: MediaSession,
        info: PlaybackInfo,
    ) {
        val player = session.player
        val recentItem = mediaItemHelper.buildMediaItem(info)
        val resumePosition = info.lastPosition.coerceAtLeast(0L)

        player.setMediaItems(listOf(recentItem), 0, resumePosition)
        player.prepare()
    }

    private suspend fun resolveMediaItem(item: MediaItem): MediaItem =
        withContext(Dispatchers.IO) {
            val hearitId = item.mediaId.toLongOrNull() ?: return@withContext item
            val info =
                UseCaseProvider.getPlaybackInfoUseCase(hearitId).getOrNull()
                    ?: return@withContext item

            val extras = item.mediaMetadata.extras
            val playbackMode = extras?.getString(EXTRA_PLAYBACK_MODE)
            val bookmarkId = extras?.getLong(EXTRA_BOOKMARK_ID, -1L)?.takeIf { it > 0 }

            mediaItemHelper.buildMediaItem(
                info = info,
                playbackMode = playbackMode,
                bookmarkId = bookmarkId,
            )
        }

    private fun <T> executeAsync(
        operationName: String,
        operation: suspend () -> T,
    ): ListenableFuture<T> =
        CallbackToFutureAdapter.getFuture { completer ->
            val job =
                serviceScope.launch {
                    runCatching { operation() }
                        .onSuccess { completer.set(it) }
                        .onFailure { completer.setException(it) }
                }
            completer.addCancellationListener({ job.cancel() }, Runnable::run)
            operationName
        }

    private data class LibraryPlayParams(
        val seedBookmarkId: Long,
        val seedHearitId: Long,
        val startPosition: Long,
    ) {
        companion object {
            fun fromBundle(bundle: Bundle): LibraryPlayParams =
                LibraryPlayParams(
                    seedBookmarkId = bundle.getLong(EXTRA_SEED_BOOKMARK_ID, -1L),
                    seedHearitId = bundle.getLong(EXTRA_SEED_HEARIT_ID, -1L),
                    startPosition = bundle.getLong(EXTRA_START_POSITION, 0L).coerceAtLeast(0L),
                )
        }
    }

    private data class LibraryLoadResult(
        val items: List<MediaItem>,
        val seedIndex: Int,
    )

    private suspend fun preRecordCurrent(
        session: MediaSession,
        minMs: Long = 1_000L,
    ) {
        val player = session.player
        val id = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        val pos = player.currentPosition.coerceAtLeast(0L)
        if (pos >= minMs) {
            withContext(Dispatchers.IO) {
                // 핵심: 여기서 PlayingHistoryRepository 호출
                RepositoryProvider.playingHistoryRepository.addPlayingHistory(id, pos)
            }
        }
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 10

        private const val ACTION_PRELOAD_RECENT = "PRELOAD_RECENT"
        private const val ACTION_START_LIBRARY_PLAY = "START_LIBRARY_PLAY"
        private const val ACTION_PREFETCH_NEXT = "PREFETCH_NEXT"

        private const val EXTRA_PLAYBACK_MODE = "PLAYBACK_MODE"
        private const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"
        private const val EXTRA_START_POSITION = "START_POSITION"
        private const val EXTRA_SEED_BOOKMARK_ID = "SEED_BOOKMARK_ID"
        private const val EXTRA_SEED_HEARIT_ID = "SEED_HEARIT_ID"

        val PRELOAD_RECENT_COMMAND = SessionCommand(ACTION_PRELOAD_RECENT, Bundle.EMPTY)
        val START_LIBRARY_PLAY_COMMAND = SessionCommand(ACTION_START_LIBRARY_PLAY, Bundle.EMPTY)
        val PREFETCH_NEXT_COMMAND = SessionCommand(ACTION_PREFETCH_NEXT, Bundle.EMPTY)

        private val EMPTY_MEDIA_ITEMS_WITH_START =
            MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0L)
    }
}
