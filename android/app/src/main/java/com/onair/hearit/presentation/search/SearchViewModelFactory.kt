package com.onair.hearit.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.di.UseCaseProvider
import com.onair.hearit.domain.model.SearchInput

@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory(
    private val input: SearchInput?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val categoryRepository = RepositoryProvider.categoryRepository
        val recentKeywordRepository = RepositoryProvider.recentKeywordRepository
        val getSearchResultUseCase = UseCaseProvider.getSearchResultUseCase
        return SearchViewModel(
            categoryRepository,
            recentKeywordRepository,
            getSearchResultUseCase,
            input,
        ) as T
    }
}
