package com.onair.hearit.service

import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AutoPrefetchController
 *
 * 플레이어의 재생 이벤트를 감지하고,
 * 다음 미디어 아이템을 미리 불러오도록(preload/prefetch) 트리거하는 역할을 담당함
 */
class AutoPrefetchController(
    private val serviceScope: CoroutineScope,
    private val player: Player,
    private val session: MediaSession,
    private val handlePrefetchNext: suspend (MediaSession) -> Unit,
) : Player.Listener {
    private var isLibraryMode: Boolean = false
    private val isPrefetching = AtomicBoolean(false)

    fun setLibraryMode(enabled: Boolean) {
        isLibraryMode = enabled
    }

    fun attach() {
        player.addListener(this)
    }

    fun detach() {
        player.removeListener(this)
    }

    override fun onEvents(
        player: Player,
        events: Player.Events,
    ) {
        val hasPrefetchTrigger =
            events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_IS_PLAYING_CHANGED) ||
                events.contains(Player.EVENT_MEDIA_METADATA_CHANGED)

        if (!hasPrefetchTrigger) return

        if (events.contains(Player.EVENT_TIMELINE_CHANGED) ||
            events.contains(Player.EVENT_POSITION_DISCONTINUITY)
        ) {
            return
        }

        if (!shouldPrefetch(player)) return

        if (!isPrefetching.compareAndSet(false, true)) return

        serviceScope.launch {
            try {
                handlePrefetchNext(session)
            } finally {
                isPrefetching.set(false)
            }
        }
    }

    // 남아있는 연속 컨텐츠가 3개 이하인 경우에 prefetch 하도록 함
    private fun shouldPrefetch(player: Player): Boolean {
        val index = player.currentMediaItemIndex
        val count = player.mediaItemCount

        if (index == -1 || count <= 0) return false
        val remaining = count - (index + 1)
        return remaining <= PREFETCH_THRESHOLD
    }

    companion object {
        private const val PREFETCH_THRESHOLD = 3
    }
}
