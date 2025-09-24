package com.onair.hearit.service

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.onair.hearit.service.PlaybackMediaItemManager.Companion.EXTRA_LAST_POSITION_MS
import kotlin.math.abs

@OptIn(UnstableApi::class)
class PlaybackPositionListener(
    private val player: Player,
    private val minResumeMs: Long = 1_000L, // 1초 미만은 복구 안 함
    private val toleranceMs: Long = 300L, // 현재 위치가 저장 위치와 이 오차 이내면 스킵
) : Player.Listener {
    private val resumedOnceById = LinkedHashSet<String>()
    private var attached = false

    fun attach() {
        if (attached) return
        player.addListener(this)
        attached = true
    }

    fun detach() {
        if (!attached) return
        player.removeListener(this)
        attached = false
        resumedOnceById.clear()
    }

    fun reset() {
        resumedOnceById.clear()
    }

    override fun onMediaItemTransition(
        item: MediaItem?,
        reason: Int,
    ) {
        val shouldConsider =
            when (reason) {
                Player.MEDIA_ITEM_TRANSITION_REASON_AUTO,
                Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT,
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED,
                Player.MEDIA_ITEM_TRANSITION_REASON_SEEK,
                -> true

                else -> false
            }
        if (!shouldConsider) return

        val id = item?.mediaId ?: return
        if (!resumedOnceById.add(id)) return

        val last =
            item.mediaMetadata.extras
                ?.getLong(EXTRA_LAST_POSITION_MS, 0L) ?: 0L
        if (last < minResumeMs) return

        val current = player.currentPosition.coerceAtLeast(0L)
        if (abs(current - last) <= toleranceMs) return

        player.seekTo(player.currentMediaItemIndex, last)
    }
}
