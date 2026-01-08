package com.onair.hearit.domain.usecase.search

import com.onair.hearit.domain.repository.RecentKeywordRepository
import javax.inject.Inject

class ClearRecentKeywordsUseCase @Inject constructor(
    private val repository: RecentKeywordRepository,
) {
    suspend operator fun invoke(): Result<Int> = repository.clearKeywords()
}
