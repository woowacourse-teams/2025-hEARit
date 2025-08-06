package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.toPlaybackInfo
import com.onair.hearit.presentation.toBearerToken

class GetPlaybackInfoUseCase(
    private val dataStoreRepository: DataStoreRepository,
    private val hearitRepository: HearitRepository,
    private val mediaFileRepository: MediaFileRepository,
    private val recentHearitRepository: RecentHearitRepository,
) {
    suspend operator fun invoke(hearitId: Long): Result<PlaybackInfo> =
        runCatching {
            val token = dataStoreRepository.getAccessToken().getOrNull()
            val hearitInfo =
                hearitRepository.getHearit(token?.toBearerToken(), hearitId).getOrThrow()
            val audioUrl = mediaFileRepository.getOriginalAudioUrl(hearitId).getOrThrow().url
            val recentHearit = recentHearitRepository.getRecentHearit().getOrThrow()
            val lastPosition = recentHearit?.lastPosition ?: 0L
            val duration = hearitInfo.playTime * 1000L

            hearitInfo.toPlaybackInfo(audioUrl, hearitInfo.title, lastPosition, duration)
        }
}
