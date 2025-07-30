package com.onair.hearit.presentation.search.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.datasource.CategoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.RecentKeywordRepositoryImpl
import com.onair.hearit.di.DatabaseProvider
import com.onair.hearit.di.NetworkProvider
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.usecase.GetSearchResultUseCase

@Suppress("UNCHECKED_CAST")
class SearchResultViewModelFactory(
    private val input: SearchInput,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val categoryRemoteDataSource = CategoryRemoteDataSourceImpl(NetworkProvider.categoryService)
        val recentKeywordDataSource =
            HearitLocalDataSourceImpl(DatabaseProvider.hearitDao, crashlyticsLogger)

        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource, crashlyticsLogger)
        val categoryRepository = CategoryRepositoryImpl(categoryRemoteDataSource, crashlyticsLogger)
        val recentKeywordRepository =
            RecentKeywordRepositoryImpl(recentKeywordDataSource, crashlyticsLogger)

        val getSearchResultUseCase =
            GetSearchResultUseCase(
                hearitRepository = hearitRepository,
                categoryRepository = categoryRepository,
            )

        return SearchResultViewModel(recentKeywordRepository, getSearchResultUseCase, input) as T
    }
}
