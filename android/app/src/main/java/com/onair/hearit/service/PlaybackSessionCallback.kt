package com.onair.hearit.service

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import com.onair.hearit.di.RepositoryProvider.playingHistoryRepository
import com.onair.hearit.presentation.executeAsync
import com.onair.hearit.service.model.LibraryPlayParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@UnstableApi
class PlaybackSessionCallback(
    private val serviceScope: CoroutineScope,
    private val mediaItemManager: PlaybackMediaItemManager,
    private val libraryPlaybackHandler: LibraryPlaybackHandler,
    private val recentPlaybackHandler: RecentPlaybackHandler,
) : MediaSession.Callback {
    // 컨트롤러가 세션에 연결될 때 호출됨
    // 기본 세션 명령어 + 커스텀 명령어(PRELOAD, START_LIBRARY_PLAY, PREFETCH_NEXT)를 등록
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): MediaSession.ConnectionResult {
        val baseResult = super.onConnect(session, controller)

        val sessionCommands =
            baseResult.availableSessionCommands
                .buildUpon()
                .add(PRELOAD_RECENT_COMMAND) // 최근 항목 미리 불러오기
                .add(START_LIBRARY_PLAY_COMMAND) // 라이브러리 재생 시작
                .add(PREFETCH_NEXT_COMMAND) // 다음 페이지 미리 가져오기
                .build()

        return MediaSession.ConnectionResult
            .AcceptedResultBuilder(session)
            .setAvailableSessionCommands(sessionCommands)
            .setAvailablePlayerCommands(baseResult.availablePlayerCommands)
            .build()
    }

    // 컨트롤러가 미디어 아이템을 지정했을 때 호출됨
    // 현재 재생중인 상태 기록 → 아이템 resolve → 시작 인덱스/포지션 계산
    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
        executeAsync(serviceScope, "onSetMediaItems") {
            preRecordCurrent(mediaSession)
            processMediaItems(mediaItems, startIndex, startPositionMs)
        }

    // 커스텀 명령 처리
    // - START_LIBRARY_PLAY: 라이브러리 기반 재생
    // - PREFETCH_NEXT: 다음 아이템 미리 불러오기
    // - PRELOAD_RECENT: 최근 재생 아이템 미리 세션에 준비
    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        command: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> =
        executeAsync(serviceScope, "onCustomCommand") {
            when (command.customAction) {
                ACTION_START_LIBRARY_PLAY -> handleStartLibraryPlay(session, args)
                ACTION_PREFETCH_NEXT -> handlePrefetchNext(session)
                ACTION_PRELOAD_RECENT -> recentPlaybackHandler.preloadRecentItem(session)
                else -> super.onCustomCommand(session, controller, command, args).get()
            } as SessionResult
        }

    // 세션 재개 시 호출 → 최근 재생 아이템/포지션 가져옴
    override fun onPlaybackResumption(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
        executeAsync(serviceScope, "onPlaybackResumption") {
            recentPlaybackHandler.getRecentMediaItemsWithStart()
        }

    // 새로운 아이템 추가 요청 시 → resolve 해서 반환
    override fun onAddMediaItems(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
    ): ListenableFuture<List<MediaItem>> =
        executeAsync(serviceScope, "onAddMediaItems") {
            mediaItems
                .map { item ->
                    runCatching { mediaItemManager.resolveMediaItem(item) }.getOrElse { item }
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
                    runCatching { mediaItemManager.resolveMediaItem(item) }.getOrElse { item }
                }.ifEmpty { mediaItems }

        // 인덱스가 범위를 벗어나지 않도록 보정
        val safeStartIndex =
            if (resolvedItems.isNotEmpty()) {
                startIndex.coerceIn(0, resolvedItems.size - 1)
            } else {
                0
            }

        return MediaSession.MediaItemsWithStartPosition(
            resolvedItems,
            safeStartIndex,
            startPositionMs,
        )
    }

    // 라이브러리 기반 재생 처리
    private suspend fun handleStartLibraryPlay(
        session: MediaSession,
        args: Bundle,
    ): SessionResult {
        val playParams = LibraryPlayParams.fromBundle(args)
        val loadResult = libraryPlaybackHandler.loadLibraryItemsWithIndex(playParams)

        if (loadResult.items.isEmpty()) {
            return SessionResult(SessionError.ERROR_BAD_VALUE)
        }

        preRecordCurrent(session)

        withContext(Dispatchers.Main) {
            session.player.setMediaItems(
                loadResult.items,
                loadResult.seedIndex,
                session.player.currentPosition,
            )
            session.player.prepare()
            session.player.play()
        }

        return SessionResult(SessionResult.RESULT_SUCCESS)
    }

    // 다음 페이지 아이템 미리 가져오기
    private suspend fun handlePrefetchNext(session: MediaSession): SessionResult {
        val (newItems, _) = libraryPlaybackHandler.prefetchNextPage()

        if (newItems.isEmpty()) {
            return SessionResult(SessionResult.RESULT_SUCCESS)
        }

        withContext(Dispatchers.Main) {
            session.player.addMediaItems(newItems)
        }

        return SessionResult(SessionResult.RESULT_SUCCESS)
    }

    // 새로운 미디어 아이템으로 변경하기 전에 아이템의 재생 기록을 저장함
    private suspend fun preRecordCurrent(
        session: MediaSession,
        minMs: Long = 1_000L, // 최소 기록 조건(1초 이상 재생 시만 기록)
    ) {
        val player = session.player
        val id = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        val pos = player.currentPosition.coerceAtLeast(0L)
        if (pos >= minMs) {
            withContext(Dispatchers.IO) {
                playingHistoryRepository.addPlayingHistory(id, pos)
            }
        }
    }

    companion object {
        private const val ACTION_PRELOAD_RECENT = "PRELOAD_RECENT"
        private const val ACTION_START_LIBRARY_PLAY = "START_LIBRARY_PLAY"
        private const val ACTION_PREFETCH_NEXT = "PREFETCH_NEXT"

        val PRELOAD_RECENT_COMMAND = SessionCommand(ACTION_PRELOAD_RECENT, Bundle.EMPTY)
        val START_LIBRARY_PLAY_COMMAND = SessionCommand(ACTION_START_LIBRARY_PLAY, Bundle.EMPTY)
        val PREFETCH_NEXT_COMMAND = SessionCommand(ACTION_PREFETCH_NEXT, Bundle.EMPTY)
    }
}
