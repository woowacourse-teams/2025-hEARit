package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.toPlaybackInfo

class GetPlaybackInfoUseCase(
    private val hearitRepository: HearitRepository,
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(hearitId: Long): Result<PlaybackInfo> =
        runCatching {
            val hearitInfo = hearitRepository.getHearit(hearitId).getOrThrow()
            val audioUrl = mediaFileRepository.getOriginalAudioUrl(hearitId).getOrThrow().url

            hearitInfo.toPlaybackInfo(audioUrl, hearitInfo.title)
        }
}
