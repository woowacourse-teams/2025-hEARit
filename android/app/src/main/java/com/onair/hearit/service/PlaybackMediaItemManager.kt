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
                lastPosition = info.lastPosition,
            )
        return MediaItem
            .Builder()
            .setUri(info.audioUrl.toUri())
            .setMediaId(info.hearitId.toString())
            .setMediaMetadata(createMediaMetadata(info, extras))
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

    /** 여러 PlaybackInfo → MediaItemsWithStart (첫 아이템만 startPosition 적용됨) */
    fun toItemsWithStart(
        items: List<PlaybackInfo>,
        startIndex: Int,
        startPositionMs: Long,
    ): MediaSession.MediaItemsWithStartPosition {
        val mediaItems = items.map { buildMediaItem(it) }
        // safeIndex 설명 (쉽게):
        // - 목록이 있으면: startIndex를 0~마지막 인덱스 범위로 맞춘다 → 범위 밖 접근 방지
        // - 목록이 비었으면: lastIndex가 -1이므로 보정할 수 없어 0으로 처리
        val safeIndex =
            if (mediaItems.isNotEmpty()) startIndex.coerceIn(0, mediaItems.lastIndex) else 0
        return MediaSession.MediaItemsWithStartPosition(
            mediaItems,
            safeIndex,
            startPositionMs.coerceAtLeast(0L),
        )
    }

    private fun createExtras(
        bookmarkId: Long?,
        playbackMode: String?,
        lastPosition: Long?,
    ): Bundle =
        Bundle().apply {
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
        const val EXTRA_PLAYBACK_MODE = "PLAYBACK_MODE"
        const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"
        const val EXTRA_LAST_POSITION_MS = "LAST_POSITION_MS"
    }
}
