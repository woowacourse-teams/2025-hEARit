package com.onair.hearit.domain.usecase.search

import com.onair.hearit.domain.model.RecentSearch
import com.onair.hearit.domain.repository.RecentKeywordRepository
import javax.inject.Inject

class SaveRecentKeywordUseCase @Inject constructor(
    private val repository: RecentKeywordRepository,
) {
    suspend operator fun invoke(term: String): Result<Unit> =
        repository.saveKeyword(
            RecentSearch(
                term = term,
                searchedAt = System.currentTimeMillis(),
            ),
        )
}
