package com.onair.hearit.service

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@UnstableApi
class PlaybackMediaItemManager(
    private val getPlaybackInfoUseCase: GetPlaybackInfoUseCase,
) {
    fun buildMediaItem(
        info: PlaybackInfo,
        playbackMode: String? = null,
        bookmarkId: Long? = null,
    ): MediaItem {
        val extras =
            createExtras(
                bookmarkId = bookmarkId,
                playbackMode = playbackMode,
            )

        return MediaItem
            .Builder()
            .setUri(info.audioUrl.toUri())
            .setMediaId(info.hearitId.toString())
            .setMediaMetadata(createMediaMetadata(info, extras))
            .setTag(playbackMode)
            .build()
    }

    suspend fun resolveMediaItem(item: MediaItem): MediaItem =
        withContext(Dispatchers.IO) {
            val hearitId = item.mediaId.toLongOrNull() ?: return@withContext item
            val info =
                getPlaybackInfoUseCase(hearitId).getOrNull()
                    ?: return@withContext item

            val extras = item.mediaMetadata.extras
            val playbackMode = extras?.getString(EXTRA_PLAYBACK_MODE)
            val bookmarkId = extras?.getLong(EXTRA_BOOKMARK_ID, -1L)?.takeIf { it > 0 }

            buildMediaItem(
                info = info,
                playbackMode = playbackMode,
                bookmarkId = bookmarkId,
            )
        }

    fun toItemsWithStart(info: PlaybackInfo): MediaSession.MediaItemsWithStartPosition {
        val item = buildMediaItem(info)
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
    ): Bundle =
        Bundle().apply {
            bookmarkId?.let { putLong(EXTRA_BOOKMARK_ID, it) }
            playbackMode?.let { putString(EXTRA_PLAYBACK_MODE, it) }
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
    }
}
