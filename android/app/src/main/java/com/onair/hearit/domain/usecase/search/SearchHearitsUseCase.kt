package com.onair.hearit.domain.usecase.search

import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.repository.HearitRepository
import javax.inject.Inject

class SearchHearitsUseCase @Inject constructor(
    private val repository: HearitRepository,
) {
    suspend operator fun invoke(
        term: String,
        page: Int,
    ): Result<PageResult<SearchedHearit>> = repository.getKeywordHearits(term, page)
}
