package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import javax.inject.Inject

class GetHearitUseCase @Inject constructor(
    private val hearitRepository: HearitRepository,
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(hearitId: Long): Result<Hearit> =
        runCatching {
            val hearitInfo = hearitRepository.getHearit(hearitId).getOrThrow()
            val audioUrl = mediaFileRepository.getOriginalAudioUrl(hearitId).getOrThrow().url
            val script = mediaFileRepository.getScriptLines(hearitId).getOrThrow()

            hearitInfo.copy(audioUrl = audioUrl, script = script)
        }
}
