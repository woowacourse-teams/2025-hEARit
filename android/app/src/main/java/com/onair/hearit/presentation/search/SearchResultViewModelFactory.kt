package com.onair.hearit.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.CategoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.di.NetworkProvider
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.usecase.GetSearchResultUseCase

@Suppress("UNCHECKED_CAST")
class SearchResultViewModelFactory(
    private val input: SearchInput,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val categoryRemoteDataSource = CategoryRemoteDataSourceImpl(NetworkProvider.categoryService)

        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource)
        val categoryRepository = CategoryRepositoryImpl(categoryRemoteDataSource)

        val getSearchResultUseCase =
            GetSearchResultUseCase(
                hearitRepository = hearitRepository,
                categoryRepository = categoryRepository,
            )

        return SearchResultViewModel(getSearchResultUseCase, input) as T
    }
}
