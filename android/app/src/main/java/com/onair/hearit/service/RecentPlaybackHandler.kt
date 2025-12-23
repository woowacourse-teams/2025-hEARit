package com.onair.hearit.service

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(UnstableApi::class)
@ServiceScoped
class RecentPlaybackHandler @Inject constructor(
    private val recentHearitRepository: RecentHearitRepository,
    private val getPlaybackInfoUseCase: GetPlaybackInfoUseCase,
    private val mediaItemManager: PlaybackMediaItemManager,
) {
    // MediaSession.Callback의 onPlaybackResumption에서 호출될 함수
    suspend fun getRecentMediaItemsWithStart(): MediaSession.MediaItemsWithStartPosition {
        val recentInfo = loadRecentPlaybackInfo()
        return recentInfo?.let {
            mediaItemManager.toItemsWithStart(
                listOf(it),
                0,
                recentInfo.lastPosition ?: 0L,
            )
        }
            ?: EMPTY_MEDIA_ITEMS_WITH_START
    }

    // MediaSession.Callback의 onCustomCommand (ACTION_PRELOAD_RECENT)에서 호출될 함수
    suspend fun preloadRecentItem(session: MediaSession) {
        val recentInfo = loadRecentPlaybackInfo()
        recentInfo?.let {
            val player = session.player
            val recentItem = mediaItemManager.buildMediaItem(it)
            val resumePosition = it.lastPosition?.coerceAtLeast(0L) ?: 0L
            // 플레이어에 미디어 아이템을 세팅하고 준비시킴 (직접 플레이어 제어)
            player.playWhenReady = false
            player.setMediaItems(listOf(recentItem), 0, resumePosition)
            player.prepare()
        }
    }

    // 실제 PlaybackInfo를 로드하는 내부 함수
    private suspend fun loadRecentPlaybackInfo(): PlaybackInfo? =
        withContext(Dispatchers.IO) {
            recentHearitRepository
                .getRecentHearit()
                .getOrNull()
                ?.let { recent ->
                    getPlaybackInfoUseCase(recent.id).getOrNull()
                }
        }

    companion object {
        private val EMPTY_MEDIA_ITEMS_WITH_START =
            MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0L)
    }
}
