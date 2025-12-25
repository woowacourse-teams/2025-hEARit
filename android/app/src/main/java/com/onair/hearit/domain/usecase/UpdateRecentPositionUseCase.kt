package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.repository.RecentHearitRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRecentPositionUseCase @Inject constructor(
    private val recentHearitRepository: RecentHearitRepository,
) {
    suspend operator fun invoke(
        hearitId: Long,
        currentPosition: Long,
        duration: Long,
        isFinishedFromEvent: Boolean = false, // 외부에서 '완료' 상태를 강제할 경우
    ) {
        val isFinishedByTime = duration > 0 && currentPosition >= duration - 1000L
        val isCompleted = isFinishedFromEvent || isFinishedByTime

        val positionToSave = if (isCompleted) 0L else currentPosition

        recentHearitRepository.updateRecentHearitPosition(
            hearitId = hearitId,
            position = positionToSave,
        )
    }
}
