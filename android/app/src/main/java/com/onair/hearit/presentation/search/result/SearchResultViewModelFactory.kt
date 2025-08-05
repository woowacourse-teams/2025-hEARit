package com.onair.hearit.presentation.search.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.usecase.GetSearchResultUseCase

@Suppress("UNCHECKED_CAST")
class SearchResultViewModelFactory(
    private val input: SearchInput,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitRepository = RepositoryProvider.hearitRepository
        val categoryRepository = RepositoryProvider.categoryRepository
        val recentKeywordRepository = RepositoryProvider.recentKeywordRepository
        val getSearchResultUseCase =
            GetSearchResultUseCase(hearitRepository, categoryRepository)

        return SearchResultViewModel(
            recentKeywordRepository,
            getSearchResultUseCase,
            input,
            crashlyticsLogger,
        ) as T
    }
}
