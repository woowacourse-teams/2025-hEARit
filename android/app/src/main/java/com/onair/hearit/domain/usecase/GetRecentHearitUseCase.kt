package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.RecentHearitRepository

class GetRecentHearitUseCase(
    private val recentHearitRepository: RecentHearitRepository,
) {
    suspend operator fun invoke(): Result<RecentHearit?> = recentHearitRepository.getRecentHearit()
}
