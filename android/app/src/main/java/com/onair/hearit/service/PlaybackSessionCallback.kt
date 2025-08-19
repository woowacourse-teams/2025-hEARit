package com.onair.hearit.service

import android.os.Bundle
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.di.UseCaseProvider
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@UnstableApi
class PlaybackSessionCallback(
    private val player: Player,
    private val serviceScope: CoroutineScope,
) : MediaSession.Callback {
    // PlaybackInfo 객체 자체를 mediaItem으로 변환하는 로직을 담은 Manager를 선언해줌
    private val mediaItemHelper = PlaybackMediaItemManager()

    private val playerListener =
        object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                // 재생이 정상적으로 끝났을 때만 로직을 실행
                if (playbackState != Player.STATE_ENDED) {
                    return
                }

                // 현재 재생이 끝난 아이템을 가져옴
                val currentItem = player.currentMediaItem ?: return

                val extras = currentItem.mediaMetadata.extras
                val bookmarkId = extras?.getLong("BOOKMARK_ID", -1L) ?: -1L

                Timber.d("▶️ 북마크 ID 찾음: $bookmarkId")

                // 1단계에서 설정한 태그를 확인
                val mode = currentItem.localConfiguration?.tag as? String

                Timber.d("🧐 사용하는 쪽 mode: $mode")
                if (mode != PlayerDetailActivity.LIBRARY_SCREEN_ID || bookmarkId == -1L) {
                    return
                }

                // 코루틴으로 다음 북마크(재생할 아이템)를 가져옴
                serviceScope.launch {
                    Timber.d("▶️ 연속 재생 로직 시작. 현재 ID: $bookmarkId")
                    val nextBookmark =
                        UseCaseProvider.getNextBookmarkUseCase.invoke(bookmarkId).getOrNull()

                    Timber.d("✅ 다음 북마크: ${nextBookmark?.title}")
                    Timber.d("✅ 다음 북마크: ${nextBookmark?.audioUrl}")

                    if (nextBookmark != null) {
                        Timber.d("✅ 다음 북마크 찾음: ${nextBookmark.title}")
                        val playbackInfo =
                            PlaybackInfo(
                                hearitId = nextBookmark.hearitId,
                                audioUrl = nextBookmark.audioUrl ?: "",
                                title = nextBookmark.title,
                                source = "hEARit",
                            )

                        // 다음 MediaItem을 만들 때도 똑같이 라이브러리 모드 태그를 붙여줌
                        val nextMediaItem =
                            mediaItemHelper.buildMediaItem(
                                playbackInfo,
                                PlayerDetailActivity.LIBRARY_SCREEN_ID,
                            )

                        // 플레이어의 재생 목록에 다음 아이템을 추가
                        player.addMediaItem(nextMediaItem)
                        Timber.d("➕ 플레이어에 다음 아이템 추가 완료. 총 아이템 수: ${player.mediaItemCount}")

                        player.seekToNextMediaItem() // 다음 아이템으로 이동
                        player.play() // 재생 시작
                        Timber.d("⏯️ 다음 아이템으로 이동 및 재생 명령 실행!")
                    }
                }
            }
        }

    init {
        // 콜백이 생성될 때 플레이어에 리스너를 등록!
        player.addListener(playerListener)
    }

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
                .build()
        val playerCommands = base.availablePlayerCommands

        return MediaSession.ConnectionResult
            .AcceptedResultBuilder(session)
            .setAvailableSessionCommands(sessionCommands)
            .setAvailablePlayerCommands(playerCommands)
            .build()
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
    ): ListenableFuture<SessionResult> {
        if (command.customAction != COMMAND_PRELOAD_RECENT) {
            return super.onCustomCommand(session, controller, command, args)
        }
        return CallbackToFutureAdapter.getFuture { completer ->
            val job =
                serviceScope.launch {
                    try {
                        loadRecentInfo()?.let { info ->
                            prepareIfNeeded(info)
                        }
                        completer.set(SessionResult(SessionResult.RESULT_SUCCESS))
                    } catch (e: Exception) {
                        completer.setException(e)
                    }
                }
            completer.addCancellationListener({ job.cancel() }, Runnable::run)
            "preload_recent_command"
        }
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
                    try {
                        val info = loadRecentInfo()
                        val result =
                            info?.let { mediaItemHelper.toItemsWithStart(it) }
                                ?: EMPTY_MEDIA_ITEMS_WITH_START
                        completer.set(result)
                    } catch (_: Exception) {
                        completer.set(
                            EMPTY_MEDIA_ITEMS_WITH_START,
                        )
                    }
                }
            completer.addCancellationListener({ job.cancel() }, Runnable::run)
            "onPlaybackResumption"
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
    private fun prepareIfNeeded(info: PlaybackInfo) {
        val item = mediaItemHelper.buildMediaItem(info)
        val sameItem = player.currentMediaItem?.mediaId == item.mediaId
        val preparedOrBuffering =
            player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING
        if (!(sameItem && preparedOrBuffering)) {
            player.setMediaItems(listOf(item), 0, info.lastPosition)
            player.prepare()
        }
    }

    companion object {
        private const val COMMAND_PRELOAD_RECENT = "hearit.PRELOAD_RECENT"
        private val EMPTY_MEDIA_ITEMS_WITH_START =
            MediaSession.MediaItemsWithStartPosition(
                emptyList(),
                0,
                0L,
            )
        val PRELOAD_RECENT_COMMAND: SessionCommand =
            SessionCommand(COMMAND_PRELOAD_RECENT, Bundle.EMPTY)
    }
}
