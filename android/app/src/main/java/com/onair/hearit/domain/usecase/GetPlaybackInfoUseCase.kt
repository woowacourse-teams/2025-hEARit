package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.toPlaybackInfo
import javax.inject.Inject

class GetPlaybackInfoUseCase @Inject constructor(
    private val hearitRepository: HearitRepository,
    private val mediaFileRepository: MediaFileRepository,
    private val recentHearitRepository: RecentHearitRepository,
) {
    suspend operator fun invoke(hearitId: Long): Result<PlaybackInfo> =
        runCatching {
            val hearitInfo = hearitRepository.getHearit(hearitId).getOrThrow()
            val audioUrl = mediaFileRepository.getOriginalAudioUrl(hearitId).getOrThrow().url
            val recentHearit = recentHearitRepository.getRecentHearit().getOrThrow()
            val lastPosition = recentHearit?.lastPosition ?: hearitInfo.lastPlayTime
            val source = hearitInfo.sources.first().name

            hearitInfo.toPlaybackInfo(audioUrl, hearitInfo.title, lastPosition, source)
        }
}
