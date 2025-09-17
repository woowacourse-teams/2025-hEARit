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
    fun buildMediaItem(
        info: PlaybackInfo,
        playbackMode: String? = null,
        bookmarkId: Long? = null,
        startPositionMs: Long? = null,
    ): MediaItem {
        val extras =
            createExtras(
                bookmarkId = bookmarkId,
                playbackMode = playbackMode,
                startPosition = startPositionMs ?: info.lastPosition,
            )

        return MediaItem
            .Builder()
            .setUri(info.audioUrl.toUri())
            .setMediaId(info.hearitId.toString())
            .setMediaMetadata(createMediaMetadata(info, extras))
            .setTag(playbackMode)
            .build()
    }

    fun toItemsWithStart(info: PlaybackInfo): MediaSession.MediaItemsWithStartPosition {
        val item = buildMediaItem(info, startPositionMs = info.lastPosition)
        val startPosition = info.lastPosition.coerceAtLeast(0L)

        return MediaSession.MediaItemsWithStartPosition(
            listOf(item),
            0,
            startPosition,
        )
    }

    private fun createExtras(
        bookmarkId: Long?,
        playbackMode: String?,
        startPosition: Long,
    ): Bundle =
        Bundle().apply {
            bookmarkId?.let { putLong(EXTRA_BOOKMARK_ID, it) }
            playbackMode?.let { putString(EXTRA_PLAYBACK_MODE, it) }
            if (startPosition > 0L) {
                putLong(EXTRA_START_POSITION, startPosition)
            }
        }

    private fun createMediaMetadata(
        info: PlaybackInfo,
        extras: Bundle,
    ): MediaMetadata =
        MediaMetadata
            .Builder()
            .setTitle(info.title)
            .setArtist(info.source)
            .setExtras(extras)
            .build()

    companion object {
        const val EXTRA_PLAYBACK_MODE = "PLAYBACK_MODE"
        const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"
        const val EXTRA_START_POSITION = "START_POSITION"
    }
}
