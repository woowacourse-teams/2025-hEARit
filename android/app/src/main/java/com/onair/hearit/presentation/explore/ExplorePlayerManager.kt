package com.onair.hearit.presentation.explore

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ExplorePlayerManager(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val onPlaybackEnded: () -> Unit,
    private val onPositionUpdated: (Long) -> Unit,
) {
    val player by lazy {
        ExoPlayer
            .Builder(context)
            .build()
            .apply {
                addListener(playbackListener)
            }
    }
    private var scriptSyncJob: Job? = null

    private val playbackListener =
        object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    onPlaybackEnded()
                }
            }
        }

    fun playAudio(
        audioUrl: String,
        startPosition: Long = 0L,
    ) {
        val mediaItem = MediaItem.fromUri(audioUrl)
        if (startPosition > 0) {
            player.setMediaItem(mediaItem, startPosition)
        } else {
            player.setMediaItem(mediaItem)
        }
        player.prepare()
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
