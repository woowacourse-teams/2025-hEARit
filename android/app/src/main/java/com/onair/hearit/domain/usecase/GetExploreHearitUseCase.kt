package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.repository.MediaFileRepository
import javax.inject.Inject

class GetExploreHearitUseCase @Inject constructor(
    private val mediaFileRepository: MediaFileRepository,
) {
    suspend operator fun invoke(item: ExploreHearit): Result<ExploreHearit> =
        runCatching {
            val audioUrl = mediaFileRepository.getShortAudioUrl(item.id).getOrThrow().url
            val scriptList = mediaFileRepository.getScriptLines(item.id).getOrThrow()
            item.copy(audioUrl = audioUrl, script = scriptList)
        }
}
