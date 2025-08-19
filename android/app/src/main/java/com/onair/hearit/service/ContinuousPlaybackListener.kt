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
import timber.log.Timber

/**
 * 라이브러리에서 시작된 미디어의 연속 재생 로직을 처리하는 [Player.Listener] 구현체입니다.
 *
 * 이 리스너는 현재 재생 중인 트랙이 자연스럽게 종료되었을 때,
 * 해당 트랙이 라이브러리 재생 모드인지 확인하고, 유효한 북마크 ID를 가지고 있다면
 * 다음 북마크 트랙을 찾아 재생 목록에 추가하고 재생을 시작합니다.
 *
 * @property player 현재 미디어를 제어하는 [Player] 인스턴스.
 * @property serviceScope 백그라운드 작업을 수행하기 위한 [CoroutineScope].
 * @property mediaItemHelper [PlaybackInfo]를 [MediaItem]으로 변환하는 헬퍼 클래스.
 */
@OptIn(UnstableApi::class)
class ContinuousPlaybackListener(
    private val player: Player,
    private val serviceScope: CoroutineScope,
    private val mediaItemHelper: PlaybackMediaItemManager,
) : Player.Listener {
    /**
     * 플레이어의 상태가 변경될 때마다 호출됩니다.
     *
     * 이 함수는 재생 상태가 [Player.STATE_ENDED]로 변경된 경우에만 연속 재생 로직을 트리거합니다.
     *
     * @param playbackState 변경된 플레이어의 현재 상태.
     */
    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState != Player.STATE_ENDED) {
            return
        }

        val currentItem = player.currentMediaItem ?: return
        val extras = currentItem.mediaMetadata.extras

        val bookmarkId =
            extras?.getLong(KEY_BOOKMARK_ID, INVALID_BOOKMARK_ID) ?: INVALID_BOOKMARK_ID
        val mode = currentItem.localConfiguration?.tag as? String

        if (mode != PlayerDetailActivity.LIBRARY_SCREEN_ID || bookmarkId == INVALID_BOOKMARK_ID) {
            return
        }

        serviceScope.launch {
            val nextBookmark = UseCaseProvider.getNextBookmarkUseCase(bookmarkId).getOrNull()

            if (nextBookmark != null && !nextBookmark.audioUrl.isNullOrEmpty()) {
                val playbackInfo =
                    PlaybackInfo(
                        hearitId = nextBookmark.hearitId,
                        audioUrl = nextBookmark.audioUrl,
                        title = nextBookmark.title,
                        source = "hEARit",
                    )

                // 1. 다음 트랙에 대한 기본 MediaItem을 생성합니다.
                var nextMediaItem =
                    mediaItemHelper.buildMediaItem(
                        playbackInfo,
                        PlayerDetailActivity.LIBRARY_SCREEN_ID,
                    )

                // 2. 다음 연속 재생을 위해, 새로 만들어질 MediaItem에도 북마크 ID를 포함시킵니다.
                val nextExtras =
                    Bundle().apply {
                        putLong(KEY_BOOKMARK_ID, nextBookmark.bookmarkId)
                    }

                // 3. 기존 MediaItem의 복사본을 만들고, 새로운 MediaMetadata(extras 포함)로 교체합니다.
                nextMediaItem =
                    nextMediaItem
                        .buildUpon()
                        .setMediaMetadata(
                            nextMediaItem.mediaMetadata
                                .buildUpon()
                                .setExtras(nextExtras)
                                .build(),
                        ).build()

                // 4. 완성된 MediaItem을 플레이어의 재생 목록에 추가합니다.
                player.addMediaItem(nextMediaItem)

                // 5. 조건 없이 다음 트랙으로 이동하고 재생을 시작하라고 명시적으로 명령합니다.
                player.seekToNextMediaItem()
                player.play()
            } else {
                Timber.w("❌ 다음 북마크 또는 audioUrl이 없어 연속 재생을 중단합니다.")
            }
        }
    }

    companion object {
        /**
         * MediaItem의 extras [Bundle]에서 북마크 ID를 식별하기 위한 키 값입니다.
         * 이 값은 [PlaybackService]에서 MediaItem을 생성할 때 사용하는 키와 반드시 일치해야 합니다.
         */
        private const val KEY_BOOKMARK_ID = "BOOKMARK_ID"

        /**
         * 유효하지 않은 북마크 ID를 나타내는 상수 값입니다.
         */
        private const val INVALID_BOOKMARK_ID = -1L
    }
}
