package com.onair.hearit.service

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@UnstableApi
@ServiceScoped
class PlaybackMediaItemManager @Inject constructor(
    private val getPlaybackInfoUseCase: GetPlaybackInfoUseCase,
) {
    fun buildMediaItem(
        info: PlaybackInfo,
        playbackMode: String? = null,
        bookmarkId: Long? = null,
    ): MediaItem {
        val extras =
            createExtras(
                hearitId = info.hearitId,
                bookmarkId = bookmarkId,
                playbackMode = playbackMode,
                lastPosition = info.lastPosition,
            )

        val requestMetadata =
            MediaItem.RequestMetadata
                .Builder()
                .setExtras(extras)
                .build()

        return MediaItem
            .Builder()
            .setUri(info.audioUrl.toUri())
            .setMediaId(info.hearitId.toString())
            .setMediaMetadata(createMediaMetadata(info, extras))
            .setRequestMetadata(requestMetadata)
            .setTag(playbackMode)
            .build()
    }

    /** (컨트롤러 경로) 들어온 MediaItem을 최신 정보로 resolve */
    suspend fun resolveMediaItem(item: MediaItem): MediaItem =
        withContext(Dispatchers.IO) {
            val hearitId = item.mediaId.toLongOrNull() ?: return@withContext item
            val info = getPlaybackInfoUseCase(hearitId).getOrNull() ?: return@withContext item

            val extras = item.mediaMetadata.extras
            val playbackMode = extras?.getString(EXTRA_PLAYBACK_MODE)
            val bookmarkId = extras?.getLong(EXTRA_BOOKMARK_ID, -1L)?.takeIf { it > 0 }

            buildMediaItem(
                info = info,
                playbackMode = playbackMode,
                bookmarkId = bookmarkId,
            )
        }

    fun toItemsWithStart(
        items: List<PlaybackInfo>,
        startIndex: Int,
        startPositionMs: Long,
        playbackMode: String? = null,
    ): MediaSession.MediaItemsWithStartPosition {
        val mediaItems =
            items.map { info ->
                // bookmarkId와 playbackMode를 extras/tag에 함께 담는다.
                buildMediaItem(
                    info = info,
                    playbackMode = playbackMode,
                    bookmarkId = info.bookmarkId,
                )
            }

        // 안전한 시작 인덱스 계산: 목록이 있으면 0~lastIndex로 보정, 없으면 0으로 처리
        val safeIndex =
            if (mediaItems.isNotEmpty()) startIndex.coerceIn(0, mediaItems.lastIndex) else 0

        return MediaSession.MediaItemsWithStartPosition(
            mediaItems,
            safeIndex,
            startPositionMs.coerceAtLeast(0L),
        )
    }

    private fun createExtras(
        hearitId: Long?,
        bookmarkId: Long?,
        playbackMode: String?,
        lastPosition: Long?,
    ): Bundle =
        Bundle().apply {
            hearitId?.let { putLong(EXTRA_HEARIT_ID, it) }
            bookmarkId?.let { putLong(EXTRA_BOOKMARK_ID, it) }
            playbackMode?.let { putString(EXTRA_PLAYBACK_MODE, it) }
            lastPosition?.let { putLong(EXTRA_LAST_POSITION_MS, it) }
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
        const val EXTRA_HEARIT_ID = "HEARIT_ID"
        const val EXTRA_PLAYBACK_MODE = "PLAYBACK_MODE"
        const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"
        const val EXTRA_LAST_POSITION_MS = "LAST_POSITION_MS"
    }
}
