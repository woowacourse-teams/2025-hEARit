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
import timber.log.Timber

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
                        // 1) 정규화(resolve). 실패 시 원본 사용
                        val resolved = mediaItems.map { resolve(it) }
                        val finalItems = resolved.ifEmpty { mediaItems }

                        // 2) startIndex 보존 (범위 안전화)
                        val finalStartIndex =
                            startIndex.coerceIn(0, (finalItems.size - 1).coerceAtLeast(0))

                        // 3) 시작 위치: 파라미터 우선, 없으면 "시작 아이템의 extras"에서
                        val extrasStart =
                            finalItems
                                .getOrNull(finalStartIndex)
                                ?.mediaMetadata
                                ?.extras
                                ?.getLong(EXTRA_START_POSITION, -1L)
                                ?: -1L

                        val finalStartPosition =
                            (
                                if (startPositionMs > 0) startPositionMs else extrasStart
                            ).coerceAtLeast(0L)

                        completer.set(
                            MediaSession.MediaItemsWithStartPosition(
                                finalItems,
                                finalStartIndex,
                                finalStartPosition,
                            ),
                        )
                    } catch (_: Throwable) {
                        // 4) 완전 실패시에도 "빈 리스트" 대신 최소한 원본으로 복구
                        completer.set(
                            MediaSession.MediaItemsWithStartPosition(
                                mediaItems,
                                0,
                                0L,
                            ),
                        )
                    }
                }
            completer.addCancellationListener({ job.cancel() }, { it.run() })
            "onSetMediaItems"
        }

    /**
     * 외부 컨트롤러로부터 커스텀 명령을 받을 때 호출됨 + 외부에서 호출할때 'hearit.PRELOAD_RECENT'를 전달함
     * 처음에 앱을 실행할때, 마지막에 저장된 위치와 더불어서 아이템을 미리 화면에 뿌려주기 위함
     * 비동기적으로 최근 재생 정보를 불러와 ExoPlayer를 준비
     */
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
                                Timber.d("library")
                                val limitFromArgs = args.getInt(EXTRA_LIMIT, 10)
                                pageSize = limitFromArgs

                                val firstPage =
                                    UseCaseProvider
                                        .getBookmarksUseCase(page = 0, size = pageSize)
                                        .getOrThrow()

                                Timber.d("library $firstPage")

                                val items =
                                    firstPage.items.map { bookmark ->
                                        mediaItemHelper.buildMediaItem(
                                            info =
                                                PlaybackInfo(
                                                    hearitId = bookmark.hearitId,
                                                    audioUrl = bookmark.audioUrl!!,
                                                    title = bookmark.title,
                                                    source = "hEARit",
                                                ),
                                            playbackMode = "LIBRARY",
                                            bookmarkId = bookmark.bookmarkId,
                                        )
                                    }

                                // 다음 페이지 인덱스 갱신
                                nextPage =
                                    if (!firstPage.paging.isLast) {
                                        firstPage.paging.page + 1
                                    } else {
                                        null
                                    }

                                withContext(Dispatchers.Main) {
                                    session.player.setMediaItems(items, 0, 0L)
                                    session.player.prepare()
                                    session.player.play()
                                }
                                SessionResult(SessionResult.RESULT_SUCCESS)
                            }

                            // 다음 페이지 프리패치
                            CMD_PREFETCH_NEXT -> {
                                val pageToLoad =
                                    nextPage
                                        ?: return@runCatching SessionResult(SessionResult.RESULT_SUCCESS)

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
                                                    audioUrl = bookmark.audioUrl!!,
                                                    title = bookmark.title,
                                                    source = "hEARit",
                                                ),
                                            playbackMode = "LIBRARY",
                                            bookmarkId = bookmark.bookmarkId,
                                        )
                                    }

                                // 다음 페이지 인덱스 갱신
                                nextPage =
                                    if (!next.paging.isLast) {
                                        next.paging.page + 1
                                    } else {
                                        null
                                    }

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
                                    .onCustomCommand(session, controller, command, args)
                                    .get()
                        }
                    }.onSuccess(completer::set).onFailure(completer::setException)
                }
            completer.addCancellationListener({ job.cancel() }, Runnable::run)
            "customCommand"
        }

    /**
     * 앱이 종료된 후 사용자가 미디어 알림에서 재생 버튼을 눌렀을 때 호출됨
     * 이 메서드는 비동기적으로 최근 재생 정보를 불러와 해당 미디어 아이템과 마지막 재생 위치를 반환합니다.
     */
    override fun onPlaybackResumption(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
        CallbackToFutureAdapter.getFuture { completer ->
            val job =
                serviceScope.launch {
                    val info = runCatching { loadRecentInfo() }.getOrNull()
                    val result =
                        info?.let {
                            mediaItemHelper.toItemsWithStart(it)
                        } ?: run {
                            EMPTY_MEDIA_ITEMS_WITH_START
                        }
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
                    }.onSuccess(completer::set)
                        .onFailure(completer::setException)
                }
            completer.addCancellationListener({ job.cancel() }, Runnable::run)
            "onAddMediaItems"
        }

    // 데이터베이스에서 최근 재생 정보를 비동기적으로 불러오는 부분으로
    // 처음에 앱을 재생할 때 마지막까지 들은 히어릿을 불러오는 코드
    private suspend fun loadRecentInfo(): PlaybackInfo? =
        withContext(Dispatchers.IO) {
            RepositoryProvider.recentHearitRepository
                .getRecentHearit()
                .getOrNull()
                ?.let { recent ->
                    UseCaseProvider.getPlaybackInfoUseCase(recent.id).getOrNull()
                }
        }

    // 현재 재생 중인 미디어 아이템과 준비 상태를 확인하여,
    // 동일하지 않은 경우 아이템인 경우 새로운 미디어 아이템으로 설정하고 플레이어를 준비시킴
    private fun prepareIfNeeded(
        session: MediaSession,
        info: PlaybackInfo,
    ) {
        val player = session.player
        val item = mediaItemHelper.buildMediaItem(info)
        val sameItem = player.currentMediaItem?.mediaId == item.mediaId
        val preparedOrBuffering =
            player.playbackState == Player.STATE_READY ||
                player.playbackState == Player.STATE_BUFFERING
        if (!(sameItem && preparedOrBuffering)) {
            player.setMediaItems(listOf(item), 0, info.lastPosition)
            player.prepare()
        }
    }

    private suspend fun resolve(item: MediaItem): MediaItem =
        withContext(Dispatchers.IO) {
            val id = item.mediaId.toLongOrNull() ?: return@withContext item
            val info =
                UseCaseProvider.getPlaybackInfoUseCase(id).getOrNull() ?: return@withContext item
            mediaItemHelper.buildMediaItem(info)
        }

    companion object {
        private const val COMMAND_PRELOAD_RECENT = "PRELOAD_RECENT"
        private const val EXTRA_START_POSITION = "START_POSITION"
        private val EMPTY_MEDIA_ITEMS_WITH_START =
            MediaSession.MediaItemsWithStartPosition(
                emptyList(),
                0,
                0L,
            )

        private const val CMD_START_LIBRARY_PLAY = "START_LIBRARY_PLAY"
        private const val CMD_PREFETCH_NEXT = "PREFETCH_NEXT"
        private const val EXTRA_MODE = "MODE"
        private const val EXTRA_SEED_BOOKMARK_ID = "SEED_BOOKMARK_ID"
        private const val EXTRA_LIMIT = "LIMIT"

        val START_LIBRARY_PLAY: SessionCommand =
            SessionCommand(CMD_START_LIBRARY_PLAY, Bundle.EMPTY)
        val PREFETCH_NEXT: SessionCommand = SessionCommand(CMD_PREFETCH_NEXT, Bundle.EMPTY)
        val PRELOAD_RECENT_COMMAND: SessionCommand =
            SessionCommand(COMMAND_PRELOAD_RECENT, Bundle.EMPTY)
    }
}
