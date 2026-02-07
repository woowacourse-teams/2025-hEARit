package com.onair.hearit.service

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.onair.hearit.di.ServiceCoroutineScope
import com.onair.hearit.domain.usecase.PostHearitViewUseCase
import com.onair.hearit.service.PlaybackMediaItemManager.Companion.EXTRA_HEARIT_ID
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ServiceScoped
class PlaybackDurationTracker @Inject constructor(
    private val postHearitView: PostHearitViewUseCase,
    @ServiceCoroutineScope private val scope: CoroutineScope,
) : Player.Listener {
    private var player: Player? = null
    private var trackingJob: Job? = null

    private data class TrackingState(
        val mediaId: String?,
        var accumulatedMs: Long = 0L,
        var isHistorySent: Boolean = false,
    )

    private var currentState: TrackingState? = null

    fun attach(player: Player) {
        this.player = player
        this.currentState = TrackingState(player.currentMediaItem?.mediaId)
        player.addListener(this)
        updateTrackingState()
    }

    fun detach() {
        stopTracking()
        player?.removeListener(this)
        player = null
        currentState = null
    }

    private fun updateTrackingState() {
        val state = currentState ?: return
        val player = player ?: return

        with(player) {
            if (isPlaying && !state.isHistorySent && currentMediaItem != null) {
                startTracking()
            } else {
                stopTracking()
            }
        }
    }

    private fun startTracking() {
        if (trackingJob?.isActive == true) return

        trackingJob =
            scope.launch {
                while (isActive) {
                    delay(CHECK_INTERVAL_MS)

                    currentState?.let { state ->
                        state.accumulatedMs += CHECK_INTERVAL_MS

                        if (state.accumulatedMs >= TARGET_DURATION_MS) {
                            sendViewHistory(state)
                            stopTracking()
                        }
                    }
                }
            }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    @OptIn(UnstableApi::class)
    private fun sendViewHistory(state: TrackingState) {
        val hearitId =
            player
                ?.currentMediaItem
                ?.requestMetadata
                ?.extras
                ?.getLong(EXTRA_HEARIT_ID, -1L)
                ?.takeIf { it != -1L } ?: return

        state.isHistorySent = true

        scope.launch {
            runCatching {
                postHearitView(hearitId)
            }.onFailure { e ->
                Timber.d("hearit 조회수 전송에 실패했습니다")
                state.isHistorySent = false
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        updateTrackingState()
    }

    override fun onMediaItemTransition(
        mediaItem: MediaItem?,
        reason: Int,
    ) {
        // 곡이 바뀌면 상태를 새 객체로 교체하여 시간과 전송 여부를 초기화
        currentState = TrackingState(mediaItem?.mediaId)
        stopTracking()
        updateTrackingState()
    }

    companion object {
        private const val TARGET_DURATION_MS = 15_000L
        private const val CHECK_INTERVAL_MS = 1_000L
    }
}
