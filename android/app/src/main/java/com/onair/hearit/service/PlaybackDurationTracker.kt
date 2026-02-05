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
    private var viewHistoryJob: Job? = null
    private var currentMediaId: String? = null

    private var accumulatedMs = 0L
    private var isHistorySent = false
    private var lastCheckTime = 0L

    private val targetMs = 15_000L
    private val checkInterval = 1_000L

    fun attach(player: Player) {
        this.player = player
        this.currentMediaId = player.currentMediaItem?.mediaId
        player.addListener(this)
        updateTrackingState()
    }

    fun detach() {
        stopTracking()
        viewHistoryJob?.cancel()
        viewHistoryJob = null
        player?.removeListener(this)
        player = null
        currentMediaId = null
        accumulatedMs = 0L
        isHistorySent = false
    }

    private fun updateTrackingState() {
        val player = this.player ?: return
        val isPlaying = player.isPlaying
        val hasValidMedia = player.currentMediaItem != null

        if (isPlaying && !isHistorySent && hasValidMedia) {
            startTracking()
        } else {
            stopTracking()
        }
    }

    private fun startTracking() {
        if (trackingJob?.isActive == true) return

        lastCheckTime = System.currentTimeMillis()

        trackingJob =
            scope.launch {
                while (isActive) {
                    delay(checkInterval)

                    val now = System.currentTimeMillis()
                    val actualElapsed = now - lastCheckTime
                    lastCheckTime = now

                    accumulatedMs += actualElapsed

                    if (accumulatedMs >= targetMs) {
                        sendViewHistory()
                        stopTracking()
                        break
                    }
                }
            }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    @OptIn(UnstableApi::class)
    private fun sendViewHistory() {
        val hearitId =
            player
                ?.currentMediaItem
                ?.requestMetadata
                ?.extras
                ?.getLong(EXTRA_HEARIT_ID, -1L) ?: return

        isHistorySent = true

        viewHistoryJob?.cancel()
        viewHistoryJob =
            scope.launch {
                runCatching {
                    postHearitView(hearitId)
                }.onFailure { e ->
                    Timber.d("hearit 조회수 전송에 실패했습니다")
                    isHistorySent = false
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
        val newId = mediaItem?.mediaId
        if (currentMediaId != newId) {
            currentMediaId = newId
            accumulatedMs = 0L
            isHistorySent = false
            updateTrackingState()
        }
    }
}
