package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetExploreHearitUseCase @Inject constructor(
    private val mediaFileRepository: MediaFileRepository,
    private val hearitRepository: HearitRepository,
) {
    /**
     * 특정 cursorId를 기준으로 리스트를 가져와서 상세 정보(오디오, 스크립트)를 채움
     */
    suspend operator fun invoke(cursorId: Long): Result<List<ExploreHearit>> =
        runCatching {
            // Repository에서 리스트 데이터 가져오기
            val cursorResult = hearitRepository.getExploreHearits(cursorId).getOrThrow()

            // 가져온 리스트의 각 아이템들에 대해 상세 정보 채우기 (병렬 처리)
            fillExploreHearitItems(cursorResult.items)
        }

    /**
     * 이미 가지고 있는 리스트 아이템들의 상세 정보를 병렬로 로드
     */
    suspend operator fun invoke(items: List<ExploreHearit>): List<ExploreHearit> =
        coroutineScope {
            fillExploreHearitItems(items)
        }

    // 내부 공통 로직: 상세 정보(Audio, Script)를 병렬로 로드하여 결합
    private suspend fun fillExploreHearitItems(items: List<ExploreHearit>): List<ExploreHearit> =
        coroutineScope {
            items
                .map { item ->
                    async(Dispatchers.IO) {
                        runCatching {
                            val audioUrl =
                                mediaFileRepository.getShortAudioUrl(item.id).getOrThrow().url
                            val scriptList =
                                mediaFileRepository.getScriptLines(item.id).getOrThrow()
                            item.copy(audioUrl = audioUrl, script = scriptList)
                        }.getOrNull()
                    }
                }.awaitAll()
                .mapNotNull { it }
        }
}
