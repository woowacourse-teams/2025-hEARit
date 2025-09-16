package com.onair.hearit.service

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.onair.hearit.domain.model.PlaybackInfo

@UnstableApi
class PlaybackMediaItemManager {
    /**
     * PlaybackInfo -> MediaItem
     * - bookmarkId / playbackMode 를 extras에 포함
     * - startPositionMs가 주어지면 그 값을, 없으면 info.lastPosition을 START_POSITION 으로 포함
     */
    fun buildMediaItem(
        info: PlaybackInfo,
        playbackMode: String? = null,
        bookmarkId: Long? = null,
        startPositionMs: Long? = null,
    ): MediaItem {
        val extras =
            Bundle().apply {
                bookmarkId?.let { putLong(EXTRA_BOOKMARK_ID, it) }
                playbackMode?.let { putString(EXTRA_PLAYBACK_MODE, it) }
                val start = (startPositionMs ?: info.lastPosition)
                if (start > 0L) putLong(EXTRA_START_POSITION, start)
            }

        return MediaItem
            .Builder()
            .setUri(info.audioUrl.toUri())
            .setMediaId(info.hearitId.toString())
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(info.title)
                    .setArtist(info.source)
                    .setExtras(extras)
                    .build(),
            ).setTag(playbackMode)
            .build()
    }

    /** 이어듣기(재시작) 용: MediaItemsWithStartPosition 생성 */
    fun toItemsWithStart(info: PlaybackInfo): MediaSession.MediaItemsWithStartPosition =
        MediaSession.MediaItemsWithStartPosition(
            listOf(buildMediaItem(info, startPositionMs = info.lastPosition)),
            0,
            info.lastPosition.coerceAtLeast(0L),
        )

    companion object {
        const val EXTRA_PLAYBACK_MODE = "PLAYBACK_MODE"
        const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"
        const val EXTRA_START_POSITION = "START_POSITION"
    }
}
