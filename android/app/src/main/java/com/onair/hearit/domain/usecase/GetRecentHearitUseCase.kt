package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.RecentHearitRepository
import javax.inject.Inject

class GetRecentHearitUseCase @Inject constructor(
    private val recentHearitRepository: RecentHearitRepository,
) {
    suspend operator fun invoke(): Result<RecentHearit?> = recentHearitRepository.getRecentHearit()
}
