package com.onair.hearit.service

import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
        if (!isLibraryMode) return

        // 아이템이 변경되거나, 갑자기 재생 위치가 변경도거나, 재생 목록이 변경될 때 먹게 이벤트를 실행하게 된다면,
        // 이미 넘어가고 나서 이벤트를 처리하거나 하는 중복적인 오류가 있을 수 있어서 이벤트를 많이 사용하지 않도록 처리하기 위함
        if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
            events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
            events.contains(Player.EVENT_TIMELINE_CHANGED)
        ) {
            return
        }

        if (!shouldPrefetch(player)) return

        serviceScope.launch {
            handlePrefetchNext(session)
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
