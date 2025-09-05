package com.onair.hearit.service

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.onair.hearit.di.UseCaseProvider
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 북마크 기반 연속 재생을 담당하는 [Player.Listener].
 *
 * 동작 원리:
 * - 플레이어가 [Player.STATE_ENDED]에 도달하면 현재 아이템의 `BOOKMARK_ID`를 확인
 * - 북마크 ID가 유효하면 다음 북마크를 조회 후 큐에 추가
 * - 안정적으로 전환하기 위해 `seekToNext → prepare → play` 순으로 호출
 *
 * 장점:
 * - ENDED 상태에서 발생할 수 있는 레이스를 prepare()로 보완
 */
@OptIn(UnstableApi::class)
class ContinuousPlaybackListener(
    private val player: Player,
    private val serviceScope: CoroutineScope,
    private val mediaItemHelper: PlaybackMediaItemManager,
) : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState != Player.STATE_ENDED) return

        val currentItem = player.currentMediaItem ?: return
        val bookmarkId =
            currentItem.mediaMetadata.extras?.getLong(KEY_BOOKMARK_ID, INVALID_BOOKMARK_ID)
                ?: INVALID_BOOKMARK_ID
        val mode = currentItem.localConfiguration?.tag as? String

        if (mode != PlayerDetailActivity.LIBRARY_SCREEN_ID || bookmarkId == INVALID_BOOKMARK_ID) {
            return
        }

        serviceScope.launch {
            val nextBookmark =
                UseCaseProvider.getNextBookmarkUseCase(bookmarkId).getOrNull()
                    ?: return@launch

            if (nextBookmark.audioUrl.isNullOrEmpty()) return@launch

            val playbackInfo =
                PlaybackInfo(
                    hearitId = nextBookmark.hearitId,
                    audioUrl = nextBookmark.audioUrl,
                    title = nextBookmark.title,
                    source = "hEARit",
                )

            var nextItem = mediaItemHelper.buildMediaItem(playbackInfo, LIBRARY_SOURCE_TAG)

            val extras =
                Bundle().apply {
                    putLong(KEY_BOOKMARK_ID, nextBookmark.bookmarkId)
                    putString(KEY_PLAYBACK_MODE, LIBRARY_SOURCE_TAG)
                }
            nextItem =
                nextItem
                    .buildUpon()
                    .setMediaMetadata(
                        nextItem.mediaMetadata
                            .buildUpon()
                            .setExtras(extras)
                            .build(),
                    ).build()

            player.addMediaItem(nextItem)

            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
            } else {
                player.seekTo(player.mediaItemCount - 1, 0L)
            }

            player.prepare()
            player.play()
        }
    }

    companion object {
        private const val KEY_BOOKMARK_ID = "BOOKMARK_ID"
        private const val KEY_PLAYBACK_MODE = "PLAYBACK_MODE"
        private const val INVALID_BOOKMARK_ID = -1L
        private const val LIBRARY_SOURCE_TAG = "LIBRARY"
    }
}
