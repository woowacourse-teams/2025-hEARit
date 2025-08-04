package com.onair.hearit.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory(
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val categoryRepository = RepositoryProvider.categoryRepository
        val recentKeywordRepository = RepositoryProvider.recentKeywordRepository
        return SearchViewModel(categoryRepository, recentKeywordRepository, crashlyticsLogger) as T
    }
}
