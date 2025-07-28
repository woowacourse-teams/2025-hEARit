package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.ShortsHearit
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.toHearitShorts

class GetShortsHearitUseCase(
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(item: RandomHearit): Result<ShortsHearit> =
        runCatching {
            val audioUrl = mediaFileRepository.getShortAudioUrl(item.id).getOrThrow().url
            val scriptList = mediaFileRepository.getScriptLines(item.id).getOrThrow()
            item.toHearitShorts(audioUrl, scriptList)
        }
}
