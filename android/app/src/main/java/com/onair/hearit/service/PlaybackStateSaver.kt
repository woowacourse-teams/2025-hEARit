package com.onair.hearit.service

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
    private var service: PlaybackService?,
) {
    private var saveJob: Job? = null

    /** 지금 재생 중인 아이템을 ‘minRecordMs 이상’ 들었으면 히스토리 기록 */
    fun recordCurrent(minRecordMs: Long = 1_000L) {
        val currentId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        val playedMs = player.currentPosition.coerceAtLeast(0L)
        if (playedMs >= minRecordMs) recordHistory(currentId, playedMs)
    }

    /** 다음 아이템으로 바꾸기 ‘직전’에 호출 → 현재 곡 기록 (중복 방지 포함) */
    fun recordBeforeSwitchingTo(
        nextId: Long,
        minRecordMs: Long = 1_000L,
    ) {
        val currentId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        if (currentId == nextId) return
        val playedMs = player.currentPosition.coerceAtLeast(0L)
        if (playedMs >= minRecordMs) recordHistory(currentId, playedMs)
    }

    suspend fun flushNowBlocking() {
        val (id, lastPos) =
            withContext(Dispatchers.Main) {
                val currentId =
                    player.currentMediaItem?.mediaId?.toLongOrNull() ?: return@withContext null
                val pos = player.currentPosition.coerceAtLeast(0L)
                currentId to pos
            } ?: return

        // IO에서 저장 (완료까지 대기)
        withContext(Dispatchers.IO) {
            runCatching {
                RepositoryProvider.recentHearitRepository.updateRecentHearitPosition(id, lastPos)
                if (lastPos >= 1_000L) {
                    RepositoryProvider.playingHistoryRepository.addPlayingHistory(id, lastPos)
                }
            }
        }
    }

    // 30초 주기 최근 위치 저장 + 종료/중단 처리
    val listener =
        @UnstableApi
        object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    startSavingPosition()
                } else {
                    // 일시정지 시점에 최근 위치 저장 + 히스토리도 한 번 남겨줌
                    stopSavingPosition()
                    recordCurrent(minRecordMs = 1_000L)
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    // 마지막 곡 끝난 시점: 히스토리 + 최근 위치 0으로 초기화 저장
                    recordCurrent(minRecordMs = 1_000L)
                    stopSavingPosition(finished = true)
                    service?.stopSelf()
                }
            }

            // old/new 받으면 직전 트랙을 정확히 기록 가능
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                @Player.DiscontinuityReason reason: Int,
            ) {
                val oldId = oldPosition.mediaItem?.mediaId?.toLongOrNull()
                val newId = newPosition.mediaItem?.mediaId?.toLongOrNull()
                if (oldId == null || newId == null || oldId == newId) return

                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    val playedMs = oldPosition.positionMs.coerceAtLeast(0L)
                    recordHistory(oldId, playedMs)
                    savePlaybackPosition()
                    return
                }

                if (reason == Player.DISCONTINUITY_REASON_SEEK ||
                    reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT
                ) {
                    val playedMs = oldPosition.positionMs.coerceAtLeast(0L)
                    if (playedMs >= 1_000L) recordHistory(oldId, playedMs)
                    savePlaybackPosition()
                }

                // 최근 위치 즉시 갱신
                savePlaybackPosition()
            }
        }

    private fun startSavingPosition() {
        saveJob?.cancel()
        saveJob =
            serviceScope.launch(Dispatchers.IO) {
                while (isActive) {
                    delay(30_000L)
                    savePlaybackPosition()
                }
            }
    }

    private fun stopSavingPosition(finished: Boolean = false) {
        saveJob?.cancel()
        savePlaybackPosition(finished)
    }

    fun release() {
        saveJob?.cancel()
        service?.let { player.removeListener(listener) }
        // 앱/서비스 종료 시점: 최근 위치 + 히스토리 한 번 더
        savePlaybackPosition()
        recordCurrent(minRecordMs = 1_000L)
        service = null
    }

    @OptIn(UnstableApi::class)
    private fun savePlaybackPosition(finished: Boolean = false) {
        serviceScope.launch(Dispatchers.IO) {
            var mediaId: Long? = null
            var lastPosition = 0L
            var completed = finished

            withContext(Dispatchers.Main) {
                mediaId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return@withContext
                val duration = player.duration
                val position = player.currentPosition
                completed = completed || (duration > 0 && position >= duration - 1_000)
                lastPosition = if (completed) 0L else position
            }
            mediaId?.let { id ->
                runCatching {
                    RepositoryProvider.recentHearitRepository.updateRecentHearitPosition(
                        id,
                        lastPosition,
                    )
                }
            }
        }
    }

    private fun recordHistory(
        hearitId: Long,
        lastPlayTime: Long,
    ) {
        serviceScope.launch(Dispatchers.IO) {
            runCatching {
                RepositoryProvider.playingHistoryRepository.addPlayingHistory(
                    hearitId,
                    lastPlayTime,
                )
            }
        }
    }
}
