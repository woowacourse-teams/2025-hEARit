package com.onair.hearit.service

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.onair.hearit.di.RepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaybackStateSaver(
    private val player: Player,
    private val serviceScope: CoroutineScope,
) {
    private var saveJob: Job? = null

    // 30초에 한번씩 마지막 재생 위치를 저장하기 위해서 runnable과 handler를 돌림
    val listener =
        @UnstableApi
        object : Player.Listener {
            // 현재 플레이어가 실행중인 경우, 30초에 한번씩 저장할 수 있도록 도와줌
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    // 재생 시작: 30초마다 위치를 저장하는 주기적인 작업 시작
                    startSavingPosition()
                } else {
                    // 재생 중단: 주기적인 저장 작업을 멈추고 마지막 위치를 한 번 저장
                    // 현재 플레이어가 실행중이지 않은 경우 runnable을 멈추고, playbackPosition을 저장
                    stopSavingPosition()
                }
            }

            /**
             * 플레이어의 재생 상태(준비, 버퍼링, 종료 등)가 변경될 때 호출됨
             * 재생이 종료(STATE_ENDED)되면, 주기적인 저장 작업을 멈추고 재생 위치를 0으로 초기화하여 저장
             */
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    stopSavingPosition(finished = true)
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

    // 재생 시작 시 호출: 주기적인 저장 코루틴을 시작
    private fun startSavingPosition() {
        // 기존 작업이 있다면 취소
        saveJob?.cancel()

        // 30초마다 위치를 저장하는 코루틴을 시작
        saveJob =
            serviceScope.launch(Dispatchers.IO) {
                // 코루틴이 시작되면 Active상태, 코루틴이 멈추면 Completed 상태
                while (isActive) {
                    delay(30_000L) // 30초 대기
                    savePlaybackPosition()
                }
            }
    }

    // 재생 중단 또는 종료 시 호출: 주기적인 저장 작업을 멈추고 마지막 위치를 한 번 저장
    private fun stopSavingPosition(finished: Boolean = false) {
        saveJob?.cancel()
        savePlaybackPosition(finished)
    }

    fun release() {
        saveJob?.cancel()
        savePlaybackPosition()
    }

    @OptIn(UnstableApi::class)
    private fun savePlaybackPosition(finished: Boolean = false) {
        serviceScope.launch(Dispatchers.IO) {
            var mediaId: Long? = null
            var lastPosition = 0L

            // player 접근은 Main 스레드에서 수행
            withContext(Dispatchers.Main) {
                mediaId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return@withContext
                val duration = player.duration
                val position = player.currentPosition
                lastPosition =
                    if (finished || (duration > 0 && position >= duration - 1_000)) 0L else position
            }

            mediaId?.let {
                runCatching {
                    RepositoryProvider.recentHearitRepository
                        .updateRecentHearitPosition(
                            hearitId = it,
                            position = lastPosition,
                        )
                }.onFailure {
                    Log.w("PlaybackSaver", "위치 저장 실패: ${it.message}")
                }
            }
        }
    }
}
