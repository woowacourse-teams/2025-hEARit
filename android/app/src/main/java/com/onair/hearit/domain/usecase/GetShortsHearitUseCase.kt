package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.RandomHearit
import com.onair.hearit.domain.ShortsHearit
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.toHearitShortsItem

class GetShortsHearitUseCase(
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(item: RandomHearit): Result<ShortsHearit> =
        runCatching {
            val audioUrl = mediaFileRepository.getShortAudioUrl(item.id).getOrThrow().url
            val scriptList = mediaFileRepository.getSubtitleLines(item.id).getOrThrow()
            item.toHearitShortsItem(audioUrl, scriptList)
        }
}
