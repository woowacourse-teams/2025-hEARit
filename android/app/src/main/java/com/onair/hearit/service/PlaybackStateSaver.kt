package com.onair.hearit.service

import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.onair.hearit.di.RepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaybackStateSaver(
    private val player: Player,
    private val serviceScope: CoroutineScope,
) {
    // 30초에 한번씩 마지막 재생 위치를 저장하기 위해서 runnable과 handler를 돌림
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
            // 현재 플레이어가 실행중인 경우, 30초에 한번씩 저장할 수 있도록 도와줌
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    // 재생 시작: 30초마다 위치를 저장하는 주기적인 작업 시작
                    saveHandler.post(saveRunnable)
                } else {
                    // 재생 중단: 주기적인 저장 작업을 멈추고 마지막 위치를 한 번 저장
                    // 현재 플레이어가 실행중이지 않은 경우 runnable을 멈추고, playbackPosition을 저장
                    saveHandler.removeCallbacks(saveRunnable)
                    savePlaybackPosition()
                }
            }

            /**
             * 플레이어의 재생 상태(준비, 버퍼링, 종료 등)가 변경될 때 호출됨
             * 재생이 종료(STATE_ENDED)되면, 주기적인 저장 작업을 멈추고 재생 위치를 0으로 초기화하여 저장
             */
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    saveHandler.removeCallbacks(saveRunnable)
                    savePlaybackPosition(finished = true)
                }
            }

            /**
             * 재생 위치가 불연속적으로 변경될 때(예: 디폴트 타임 바- 재생바가 변경되는 경우) 호출됩니다.
             * 이 경우 즉시 현재 위치를 저장하여 정확한 상태를 유지함
             */
            override fun onPositionDiscontinuity(reason: Int) {
                savePlaybackPosition()
            }
        }

    // 리소스 정리 및 서비스 종료 시에 호출됨. 주기적인 저장 멈추도록 하고 마지막 위치 정보를 저장함
    fun release() {
        saveHandler.removeCallbacks(saveRunnable)
        savePlaybackPosition()
    }

    @OptIn(UnstableApi::class)
    private fun savePlaybackPosition(finished: Boolean = false) {
        val mediaId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        val duration = player.duration
        val position = player.currentPosition

        // 플레이어가 끝났거나, 트랙의 마지막 1초 이내에 도달한 경우 위치를 0으로 초기화 함
        val lastPosition =
            if (finished || (duration > 0 && position >= duration - 1_000)) 0L else position

        serviceScope.launch(Dispatchers.IO) {
            RepositoryProvider.recentHearitRepository
                .updateRecentHearitPosition(
                    hearitId = mediaId,
                    position = lastPosition,
                )
        }
    }
}
