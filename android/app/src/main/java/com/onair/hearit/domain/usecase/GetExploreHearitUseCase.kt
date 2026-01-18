package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.repository.MediaFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetExploreHearitUseCase @Inject constructor(
    private val mediaFileRepository: MediaFileRepository,
) {
    // 단일 아이템 상세 정보 로드
    suspend operator fun invoke(item: ExploreHearit): Result<ExploreHearit> =
        runCatching {
            val audioUrl = mediaFileRepository.getShortAudioUrl(item.id).getOrThrow().url
            val scriptList = mediaFileRepository.getScriptLines(item.id).getOrThrow()
            item.copy(audioUrl = audioUrl, script = scriptList)
        }

    // 리스트 전체를 병렬(Parallel)로 로드
    suspend operator fun invoke(items: List<ExploreHearit>): List<ExploreHearit> =
        coroutineScope {
            items
                .map { item ->
                    async(Dispatchers.IO) {
                        invoke(item).getOrNull() // 위에서 정의한 단일 invoke 호출
                    }
                }.awaitAll()
                .mapNotNull { it }
        }
}
