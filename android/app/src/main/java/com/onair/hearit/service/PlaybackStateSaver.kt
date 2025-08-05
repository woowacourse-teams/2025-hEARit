package com.onair.hearit.service

import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.onair.hearit.di.ServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaybackStateSaver(
    private val player: Player,
    private val serviceScope: CoroutineScope,
) {
    private val saveHandler = Handler(Looper.getMainLooper())
    private val saveRunnable =
        object : Runnable {
            override fun run() {
                savePlaybackPosition()
                saveHandler.postDelayed(this, 30_000L)
            }
        }

    val listener =
        @UnstableApi
        object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    saveHandler.post(saveRunnable)
                } else {
                    saveHandler.removeCallbacks(saveRunnable)
                    savePlaybackPosition()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    saveHandler.removeCallbacks(saveRunnable)
                    savePlaybackPosition(finished = true)
                }
            }

            override fun onPositionDiscontinuity(reason: Int) {
                savePlaybackPosition()
            }
        }

    fun release() {
        saveHandler.removeCallbacks(saveRunnable)
        savePlaybackPosition()
    }

    @OptIn(UnstableApi::class)
    private fun savePlaybackPosition(finished: Boolean = false) {
        val mediaId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        val duration = player.duration
        val pos = player.currentPosition

        val toSave = if (finished || (duration > 0 && pos >= duration - 1_000)) 0L else pos

        serviceScope.launch(Dispatchers.IO) {
            ServiceProvider
                .recentHearitRepository()
                .updateRecentHearitPosition(
                    hearitId = mediaId,
                    position = toSave,
                ).onSuccess {
                    Log.d("save success", "$mediaId $toSave")
                }.onFailure {
                    Log.d("save", "$mediaId $toSave")
                }
        }
    }
}
