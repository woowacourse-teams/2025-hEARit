package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.HearitShorts
import com.onair.hearit.domain.model.RandomHearitItem
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.toHearitShortsItem

class GetShortsHearitUseCase(
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(item: RandomHearitItem): Result<HearitShorts> =
        runCatching {
            val audioUrl = mediaFileRepository.getShortAudioUrl(item.id).getOrThrow().url
            val scriptList = mediaFileRepository.getSubtitleLines(item.id).getOrThrow()
            item.toHearitShortsItem(audioUrl, scriptList)
        }
}
