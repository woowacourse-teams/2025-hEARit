package com.onair.hearit.service

import android.os.Bundle
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.di.UseCaseProvider
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
    private var pageSize: Int = 10

    /**
     * 외부 컨트롤러가 미디어 세션에 연결을 시도할 때 호출됨
     * 이 메서드는 세션이 허용하는 명령 목록에 '최근 들은 히어릿'을 미리 로드하는 커스텀 명령을 추가함
     */
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): MediaSession.ConnectionResult {
        val base = super.onConnect(session, controller)
        val sessionCommands =
            base.availableSessionCommands
                .buildUpon()
                .add(PRELOAD_RECENT_COMMAND)
                .add(START_LIBRARY_PLAY)
                .add(PREFETCH_NEXT)
                .build()
        val playerCommands = base.availablePlayerCommands

        return MediaSession.ConnectionResult
            .AcceptedResultBuilder(session)
            .setAvailableSessionCommands(sessionCommands)
            .setAvailablePlayerCommands(playerCommands)
            .build()
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
        CallbackToFutureAdapter.getFuture { completer ->
            val job =
                serviceScope.launch {
                    try {
                        // 개별 실패시 원본 유지
                        val resolved =
                            mediaItems.map { mi -> runCatching { resolve(mi) }.getOrElse { mi } }
                        val finalItems = resolved.ifEmpty { mediaItems }

                        val safeStartIndex =
                            if (finalItems.isNotEmpty()) {
                                startIndex.coerceIn(0, finalItems.size - 1)
                            } else {
                                0
                            }

                        val extrasStart =
                            finalItems
                                .getOrNull(safeStartIndex)
                                ?.mediaMetadata
                                ?.extras
                                ?.getLong(EXTRA_START_POSITION, -1L) ?: -1L

                        val safeStartPos =
                            (if (startPositionMs > 0) startPositionMs else extrasStart).coerceAtLeast(
                                0L,
                            )

                        completer.set(
                            MediaSession.MediaItemsWithStartPosition(
                                finalItems,
                                safeStartIndex,
                                safeStartPos,
                            ),
                        )
                    } catch (_: Throwable) {
                        completer.set(
                            MediaSession.MediaItemsWithStartPosition(
                                mediaItems,
                                startIndex.coerceAtLeast(0),
                                startPositionMs.coerceAtLeast(0L),
                            ),
                        )
                    }
                }
            completer.addCancellationListener({ job.cancel() }, { it.run() })
            "onSetMediaItems"
        }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        command: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> =
        CallbackToFutureAdapter.getFuture { completer ->
            val job =
                serviceScope.launch {
                    runCatching {
                        when (command.customAction) {
                            CMD_START_LIBRARY_PLAY -> {
                                val seedBookmarkId = args.getLong(EXTRA_SEED_BOOKMARK_ID, -1L)
                                val seedHearitId = args.getLong(EXTRA_SEED_HEARIT_ID, -1L)
                                val seedStartPosMs =
                                    args.getLong(EXTRA_START_POSITION, 0L).coerceAtLeast(0L)

                                // 여러 페이지 누적 로드하며 시드의 전역 인덱스 찾기
                                val allItems = mutableListOf<MediaItem>()
                                var page = 0
                                var globalIndex = -1
                                var isLast = false

                                while (true) {
                                    val pageResult =
                                        UseCaseProvider
                                            .getBookmarksUseCase(page = page, size = pageSize)
                                            .getOrThrow()

                                    val batchItems =
                                        pageResult.items.map { bookmark ->
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

                                    // seed 검사 (bookmarkId 우선, 없으면 hearitId)
                                    if (seedBookmarkId > 0) {
                                        val local =
                                            pageResult.items.indexOfFirst { it.bookmarkId == seedBookmarkId }
                                        if (local >= 0) globalIndex = allItems.size + local
                                    }
                                    if (globalIndex < 0 && seedHearitId > 0) {
                                        val local =
                                            pageResult.items.indexOfFirst { it.hearitId == seedHearitId }
                                        if (local >= 0) globalIndex = allItems.size + local
                                    }

                                    allItems += batchItems
                                    isLast = pageResult.paging.isLast
                                    nextPage = if (!isLast) pageResult.paging.page + 1 else null

                                    if (globalIndex >= 0 || isLast) break
                                    page += 1
                                }

                                if (globalIndex < 0) globalIndex = 0

                                withContext(Dispatchers.Main) {
                                    session.player.setMediaItems(
                                        allItems,
                                        globalIndex,
                                        seedStartPosMs,
                                    )
                                    session.player.prepare()
                                    session.player.play()
                                }
                                SessionResult(SessionResult.RESULT_SUCCESS)
                            }

                            CMD_PREFETCH_NEXT -> {
                                val pageToLoad =
                                    nextPage ?: return@runCatching SessionResult(
                                        SessionResult.RESULT_SUCCESS,
                                    )

                                val next =
                                    UseCaseProvider
                                        .getBookmarksUseCase(page = pageToLoad, size = pageSize)
                                        .getOrThrow()

                                val items =
                                    next.items.map { bookmark ->
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

                                nextPage = if (!next.paging.isLast) next.paging.page + 1 else null

                                withContext(Dispatchers.Main) {
                                    session.player.addMediaItems(items)
                                }
                                SessionResult(SessionResult.RESULT_SUCCESS)
                            }

                            COMMAND_PRELOAD_RECENT -> {
                                loadRecentInfo()?.let { prepareIfNeeded(session, it) }
                                SessionResult(SessionResult.RESULT_SUCCESS)
                            }

                            else ->
                                return@runCatching super
                                    .onCustomCommand(
                                        session,
                                        controller,
                                        command,
                                        args,
                                    ).get()
                        }
                    }.onSuccess(completer::set).onFailure(completer::setException)
                }
            completer.addCancellationListener({ job.cancel() }, Runnable::run)
            "customCommand"
        }

    override fun onPlaybackResumption(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
        CallbackToFutureAdapter.getFuture { completer ->
            val job =
                serviceScope.launch {
                    val info = runCatching { loadRecentInfo() }.getOrNull()
                    val result =
                        info?.let { mediaItemHelper.toItemsWithStart(it) }
                            ?: EMPTY_MEDIA_ITEMS_WITH_START
                    completer.set(result)
                }
            completer.addCancellationListener({ job.cancel() }, Runnable::run)
            "onPlaybackResumption"
        }

    override fun onAddMediaItems(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
    ): ListenableFuture<List<MediaItem>> =
        CallbackToFutureAdapter.getFuture { completer ->
            val job =
                serviceScope.launch {
                    runCatching {
                        mediaItems.map { resolve(it) }.ifEmpty { mediaItems }
                    }.onSuccess(completer::set).onFailure(completer::setException)
                }
            completer.addCancellationListener({ job.cancel() }, Runnable::run)
            "onAddMediaItems"
        }

    private suspend fun loadRecentInfo(): PlaybackInfo? =
        withContext(Dispatchers.IO) {
            RepositoryProvider.recentHearitRepository
                .getRecentHearit()
                .getOrNull()
                ?.let { recent -> UseCaseProvider.getPlaybackInfoUseCase(recent.id).getOrNull() }
        }

    /**
     * 최근 재생 정보로 준비:
     * - 동일 아이템이 큐에 있으면 해당 인덱스로 이동만
     * - 큐가 비었으면 단일 아이템으로 세팅
     * - 아니면 뒤에 붙이고 그 위치로 이동
     */
    private fun prepareIfNeeded(
        session: MediaSession,
        info: PlaybackInfo,
    ) {
        val player = session.player
        val item = mediaItemHelper.buildMediaItem(info)
        val resumePos = info.lastPosition.coerceAtLeast(0L)

        val existingIndex =
            (0 until player.mediaItemCount).indexOfFirst { player.getMediaItemAt(it).mediaId == item.mediaId }

        if (existingIndex >= 0) {
            player.seekTo(existingIndex, resumePos)
            if (player.playbackState == Player.STATE_IDLE) player.prepare()
            return
        }

        if (player.mediaItemCount == 0) {
            player.setMediaItems(listOf(item), 0, resumePos)
            player.prepare()
            return
        }

        player.addMediaItem(item)
        val newIndex = player.mediaItemCount - 1
        player.seekTo(newIndex, resumePos)
        player.prepare()
    }

    /**
     * 넘어온 MediaItem을 id로 재조회하여, 기존 extras(BOOKMARK_ID/PLAYBACK_MODE/START_POSITION)를 보존해 재구성
     */
    private suspend fun resolve(item: MediaItem): MediaItem =
        withContext(Dispatchers.IO) {
            val id = item.mediaId.toLongOrNull() ?: return@withContext item
            val info =
                UseCaseProvider.getPlaybackInfoUseCase(id).getOrNull() ?: return@withContext item

            val extras = item.mediaMetadata.extras
            val mode = extras?.getString(EXTRA_PLAYBACK_MODE)
            val bookmarkId = extras?.getLong(EXTRA_BOOKMARK_ID, -1L)?.takeIf { it > 0 }
            mediaItemHelper.buildMediaItem(
                info = info,
                playbackMode = mode,
                bookmarkId = bookmarkId,
            )
        }

    companion object {
        private const val COMMAND_PRELOAD_RECENT = "PRELOAD_RECENT"
        private const val CMD_START_LIBRARY_PLAY = "START_LIBRARY_PLAY"
        private const val CMD_PREFETCH_NEXT = "PREFETCH_NEXT"

        private const val EXTRA_PLAYBACK_MODE = "PLAYBACK_MODE"
        private const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"
        private const val EXTRA_START_POSITION = "START_POSITION"

        private const val EXTRA_SEED_BOOKMARK_ID = "SEED_BOOKMARK_ID"
        private const val EXTRA_SEED_HEARIT_ID = "SEED_HEARIT_ID"

        val START_LIBRARY_PLAY: SessionCommand =
            SessionCommand(CMD_START_LIBRARY_PLAY, Bundle.EMPTY)
        val PREFETCH_NEXT: SessionCommand = SessionCommand(CMD_PREFETCH_NEXT, Bundle.EMPTY)
        val PRELOAD_RECENT_COMMAND: SessionCommand =
            SessionCommand(COMMAND_PRELOAD_RECENT, Bundle.EMPTY)

        private val EMPTY_MEDIA_ITEMS_WITH_START =
            MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0L)
    }
}
