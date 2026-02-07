package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.repository.HearitRepository
import javax.inject.Inject

class PostHearitViewUseCase @Inject constructor(
    private val hearitRepository: HearitRepository,
) {
    suspend operator fun invoke(hearitId: Long): Result<Unit> = hearitRepository.postHearitView(hearitId)
}
