package com.onair.hearit.service

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.onair.hearit.domain.model.PlaybackInfo

@UnstableApi
class PlaybackMediaItemManager {
    fun buildMediaItem(info: PlaybackInfo): MediaItem =
        MediaItem
            .Builder()
            .setUri(info.audioUrl.toUri())
            .setMediaId(info.hearitId.toString())
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(info.title)
                    .build(),
            ).build()

    fun toItemsWithStart(info: PlaybackInfo): MediaSession.MediaItemsWithStartPosition =
        MediaSession.MediaItemsWithStartPosition(
            listOf(buildMediaItem(info)),
            0,
            info.lastPosition,
        )
}
