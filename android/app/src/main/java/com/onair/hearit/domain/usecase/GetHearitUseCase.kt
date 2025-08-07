package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.toHearit
import com.onair.hearit.presentation.toBearerToken

class GetHearitUseCase(
    private val dataStoreRepository: DataStoreRepository,
    private val hearitRepository: HearitRepository,
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(hearitId: Long): Result<Hearit> =
        runCatching {
            val token = dataStoreRepository.getAccessToken().getOrNull()
            val hearitInfo =
                hearitRepository.getHearit(token?.toBearerToken(), hearitId).getOrThrow()
            val audioUrl = mediaFileRepository.getOriginalAudioUrl(hearitId).getOrThrow().url
            val script = mediaFileRepository.getScriptLines(hearitId).getOrThrow()

            hearitInfo.toHearit(audioUrl, script)
        }
}
