package com.onair.hearit.service

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlaybackHistoryListener(
    private val player: Player,
    private val scope: CoroutineScope,
    private val repository: PlayingHistoryRepository = RepositoryProvider.playingHistoryRepository,
) : Player.Listener {
    // 서비스에서 수동으로 호출: "다음 아이템으로 바꾸기 직전"에 현재 아이템 기록
    fun recordIfSwitchingTo(
        nextId: Long,
        minRecordMs: Long = 1_000L,
    ) {
        val currentId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        if (currentId == nextId) return // 같은 아이템이면 기록 X

        val playedMs = player.currentPosition.coerceAtLeast(0L)
        if (playedMs >= minRecordMs) {
            record(currentId, playedMs)
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        @Player.DiscontinuityReason reason: Int,
    ) {
        val oldId = oldPosition.mediaItem?.mediaId?.toLongOrNull()
        val newId = newPosition.mediaItem?.mediaId?.toLongOrNull()
        if (oldId == null || newId == null) return
        val itemChanged = oldId != newId

        if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION && itemChanged) {
            val playedMs = oldPosition.positionMs.coerceAtLeast(0L)
            if (playedMs >= 1_000L) {
                record(oldId, playedMs)
            }
            return
        }

        if ((
                reason == Player.DISCONTINUITY_REASON_SEEK ||
                    reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT
            ) && itemChanged
        ) {
            val playedMs = oldPosition.positionMs.coerceAtLeast(0L)
            if (playedMs >= 1_000L) {
                record(oldId, playedMs)
            }
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        if (state == Player.STATE_ENDED) {
            val id = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
            val playedMs = player.currentPosition.coerceAtLeast(0L)
            record(id, playedMs)
        }
    }

    private fun record(
        hearitId: Long,
        lastPlayTime: Long,
    ) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                repository.addPlayingHistory(hearitId, lastPlayTime)
            }
        }
    }

    fun attach() = player.addListener(this)

    fun detach() = player.removeListener(this)
}
