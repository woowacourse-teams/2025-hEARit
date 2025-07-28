package com.onair.hearit.domain.usecase

import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.domain.repository.HearitRepository

class GetSearchResultUseCase(
    private val hearitRepository: HearitRepository,
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(
        input: SearchInput,
        page: Int,
        size: Int,
    ): Result<PageResult<SearchedHearit>> =
        when (input) {
            is SearchInput.Keyword -> hearitRepository.getSearchHearits(input.term, page, size)
            is SearchInput.Category ->
                categoryRepository.getHearitsByCategoryId(
                    input.id,
                    page,
                    size,
                )
        }
}
