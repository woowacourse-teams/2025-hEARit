package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.toPlaybackInfo

class GetPlaybackInfoUseCase(
    private val hearitRepository: HearitRepository,
    private val mediaFileRepository: MediaFileRepository,
    private val recentHearitRepository: RecentHearitRepository,
) {
    suspend operator fun invoke(hearitId: Long): Result<PlaybackInfo> =
        runCatching {
            val hearitInfo = hearitRepository.getHearit(hearitId).getOrThrow()
            val audioUrl = mediaFileRepository.getOriginalAudioUrl(hearitId).getOrThrow().url
            val recentHearit = recentHearitRepository.getRecentHearit().getOrThrow()

            val lastPosition =
                if (recentHearit?.id == hearitId) {
                    recentHearit.lastPosition
                } else {
                    0L
                }

            hearitInfo.toPlaybackInfo(audioUrl, hearitInfo.title, lastPosition)
        }
}
