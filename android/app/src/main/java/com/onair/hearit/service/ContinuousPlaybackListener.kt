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
 * 북마크 기반 "연속 재생"을 담당하는 [Player.Listener] 구현체.
 *
 * 동작 흐름(ENDED 이벤트 기반):
 * 1) 플레이어가 현재 트랙을 끝까지 재생해 [Player.STATE_ENDED] 가 되면 콜백이 호출됨
 * 2) 현재 아이템의 메타데이터(extras)에서 `BOOKMARK_ID`, `PLAYBACK_MODE` 를 꺼냄
 *    - `BOOKMARK_ID` : 어떤 북마크에서 재생 중이었는지 식별
 *    - `PLAYBACK_MODE`: 연속 재생을 허용해야 하는 출처인지 검사 (예: "LIBRARY")
 *    - (안정성 위해 tag 값도 폴백으로 참고)
 * 3) 조건(유효한 bookmarkId + 허용 모드)을 만족하면 `getNextBookmarkUseCase` 로 다음 북마크를 조회
 * 4) 다음 북마크의 오디오 URL로 MediaItem 을 생성하고, 동일한 extras(`BOOKMARK_ID`, `PLAYBACK_MODE`)를 세팅
 *    - 이렇게 해야 다음 곡이 끝났을 때도 같은 규칙으로 계속 이어짐(무한 연속 재생)
 * 5) 큐에 아이템을 추가하고, 다음 미디어로 이동 → `prepare()` → `play()` 순으로 호출
 *    - ENDED 타이밍에서 발생할 수 있는 레이스(준비 상태 등)를 `prepare()` 로 보완
 *
 * 설계 포인트:
 * - "언제 연속 재생을 허용할지" 를 extras의 `PLAYBACK_MODE` 로 통제 (tag 는 폴백용)
 * - 각 MediaItem 에 `BOOKMARK_ID` 를 심어두어 다음 북마크 조회의 기준으로 사용
 * - 끝에 도달하면 더 이상 넘어가지 않도록 UseCase 가 null 을 반환 (정상 종료)
 */
@OptIn(UnstableApi::class)
class ContinuousPlaybackListener(
    private val player: Player,
    private val serviceScope: CoroutineScope,
    private val mediaItemHelper: PlaybackMediaItemManager,
) : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        // 연속재생 트리거는 "트랙이 끝난 순간". 다른 상태에서는 아무 것도 하지 않음.
        if (playbackState != Player.STATE_ENDED) return

        // 현재 재생 중이던 미디어 아이템(방금 끝난 아이템)을 가져옴
        val currentItem = player.currentMediaItem ?: return

        // MediaMetadata.extras 는 우리가 PlaybackService/Manager에서 심어둔 커스텀 데이터 저장소
        val extras = currentItem.mediaMetadata.extras

        // 현재 아이템의 북마크 식별자. 없으면 INVALID_BOOKMARK_ID 로 처리
        val bookmarkId =
            extras?.getLong(KEY_BOOKMARK_ID, INVALID_BOOKMARK_ID) ?: INVALID_BOOKMARK_ID

        // 연속 재생 허용 모드 판별.
        // 원칙: extras 의 PLAYBACK_MODE 사용 → 없으면 tag 값으로 폴백(초기 도입/이관 상황 대응)
        val playbackModeFromExtras = extras?.getString(KEY_PLAYBACK_MODE)
        val playbackModeFromTag = currentItem.localConfiguration?.tag as? String
        val playbackMode = playbackModeFromExtras ?: playbackModeFromTag

        // 허용되는 모드(출처) 정의. 하나만 쓰면 setOf(LIBRARY_SOURCE_TAG) 로 좁혀도 됨.
        val allowedModes = setOf(LIBRARY_SOURCE_TAG, PlayerDetailActivity.LIBRARY_SCREEN_ID)

        // 필수 조건: 북마크 ID 가 유효하고, 모드가 허용 목록에 포함되어야 함
        if (bookmarkId == INVALID_BOOKMARK_ID || playbackMode !in allowedModes) return

        // 다음 아이템 조회/전환은 코루틴에서 비동기로 수행
        serviceScope.launch {
            // 현재 북마크 기준 "다음" 북마크를 조회 (없으면 마지막이라 종료)
            val nextBookmark = UseCaseProvider.getNextBookmarkUseCase(bookmarkId).getOrNull()
            if (nextBookmark == null) {
                // 더 이상 이어갈 북마크가 없으면 연속 재생 종료
                return@launch
            }

            // 다음 북마크에 재생 가능한 오디오 URL 이 있어야 함
            if (nextBookmark.audioUrl.isNullOrEmpty()) {
                // URL 이 비어있으면 건너뜀(정책에 따라 스킵 or 종료로 선택 가능)
                return@launch
            }

            // 플레이어에 넣을 정보를 Domain → Player 가 이해하는 형태로 변환하기 위한 DTO
            val playbackInfo =
                PlaybackInfo(
                    hearitId = nextBookmark.hearitId,
                    audioUrl = nextBookmark.audioUrl,
                    title = nextBookmark.title,
                    source = "hEARit", // 알림/메타에 표시될 아티스트/채널명 느낌
                )

            // PlaybackMediaItemManager 를 통해 MediaItem 생성 (여기서 tag 등 기본 세팅)
            var nextItem = mediaItemHelper.buildMediaItem(playbackInfo, LIBRARY_SOURCE_TAG)

            // 다음 Item 에도 반드시 BOOKMARK_ID & PLAYBACK_MODE 를 기록해야
            // "그 다음" 곡에서도 동일한 규칙으로 연속 재생이 지속됨
            val nextExtras =
                Bundle().apply {
                    putLong(KEY_BOOKMARK_ID, nextBookmark.bookmarkId)
                    putString(KEY_PLAYBACK_MODE, LIBRARY_SOURCE_TAG) // 모드 통일
                }

            // MediaItem 의 metadata.extras 를 덮어씌우고, tag 도 동일 값으로 맞춤(폴백/디버깅용)
            nextItem =
                nextItem
                    .buildUpon()
                    .setMediaMetadata(
                        nextItem.mediaMetadata
                            .buildUpon()
                            .setExtras(nextExtras)
                            .build(),
                    ).setTag(LIBRARY_SOURCE_TAG)
                    .build()

            // 큐의 끝에 다음 아이템을 추가
            player.addMediaItem(nextItem)

            // ENDED 상태이므로 다음으로 "이동" 필요.
            // - 아직 다음 아이템이 존재한다고 판단되면 seekToNextMediaItem()
            // - 그렇지 않으면 "마지막 index" 로 직접 이동 (방금 추가한 아이템이 마지막)
            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
            } else {
                player.seekTo(player.mediaItemCount - 1, 0L)
            }

            // 상태 준비/버퍼링 재개. ENDED 이후 즉시 play 가능하도록 보완
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
