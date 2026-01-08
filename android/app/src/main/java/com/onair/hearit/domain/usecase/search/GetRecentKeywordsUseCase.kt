package com.onair.hearit.domain.usecase.search

import com.onair.hearit.domain.model.RecentSearch
import com.onair.hearit.domain.repository.RecentKeywordRepository
import javax.inject.Inject

class GetRecentKeywordsUseCase @Inject constructor(
    private val repository: RecentKeywordRepository,
) {
    suspend operator fun invoke(): Result<List<RecentSearch>> = repository.getKeywords()
}
