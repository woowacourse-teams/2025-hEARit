package com.onair.hearit.presentation.explore

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException

class ExplorePlayerManager(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val onPlaybackEnded: () -> Unit,
    private val onPositionUpdated: (Long) -> Unit,
    private val onPlayError: () -> Unit,
) {
    val player by lazy {
        ExoPlayer
            .Builder(context)
            .build()
            .apply {
                addListener(playbackListener)
                addAnalyticsListener(analyticsListener)
            }
    }
    private var scriptSyncJob: Job? = null
    private var errorDispatched = false

    private val playbackListener =
        object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                if (errorDispatched) return

                when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
                    -> {
                        stop()
                        dispatchFatal()
                    }
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    errorDispatched = false
                    onPlaybackEnded()
                }
            }
        }

    private val analyticsListener =
        @UnstableApi
        object : AnalyticsListener {
            override fun onLoadError(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData,
                error: IOException,
                wasCanceled: Boolean,
            ) {
                if (errorDispatched) return

                // generateSequence -> 오류의 원인을 추적하거나 하는 경우 메모리 성능과 최적화를 위해 사용
                val isUnknownHost =
                    generateSequence(error as Throwable?) { it.cause }
                        .any { it is UnknownHostException }

                if (isUnknownHost) {
                    stop()
                    dispatchFatal()
                }
            }
        }

    private fun dispatchFatal() {
        stop()
        onPlayError()
        errorDispatched = true
    }

    fun playAudio(
        audioUrl: String,
        startPosition: Long = 0L,
    ) {
        errorDispatched = false

        val mediaItem = MediaItem.fromUri(audioUrl)
        if (startPosition > 0) {
            player.setMediaItem(mediaItem, startPosition)
        } else {
            player.setMediaItem(mediaItem)
        }

        player.prepare()
        player.playWhenReady = true
        startScriptSync()
    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
        scriptSyncJob?.cancel()
    }

    fun release() {
        player.release()
        scriptSyncJob?.cancel()
        scriptSyncJob = null
    }

    fun getCurrentPosition(): Long = player.currentPosition

    private fun startScriptSync() {
        scriptSyncJob?.cancel()
        scriptSyncJob =
            lifecycleScope.launch {
                while (isActive) {
                    if (player.isPlaying && player.playbackState == Player.STATE_READY) {
                        onPositionUpdated(player.currentPosition)
                    }
                    delay(200L)
                }
            }
    }
}
