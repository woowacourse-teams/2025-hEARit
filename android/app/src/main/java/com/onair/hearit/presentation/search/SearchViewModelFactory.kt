package com.onair.hearit.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.domain.model.SearchInput

@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory(
    private val input: SearchInput?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val categoryRepository = RepositoryProvider.categoryRepository
        val hearitRepository = RepositoryProvider.hearitRepository
        val recentKeywordRepository = RepositoryProvider.recentKeywordRepository
        return SearchViewModel(
            categoryRepository,
            hearitRepository,
            recentKeywordRepository,
            input,
        ) as T
    }
}
