package com.onair.hearit.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.CategoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.KeywordRemoteDataSourceImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
import com.onair.hearit.data.repository.KeywordRepositoryImpl
import com.onair.hearit.di.NetworkProvider

@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val categoryDataSource = CategoryRemoteDataSourceImpl(NetworkProvider.categoryService)
        val categoryRepository = CategoryRepositoryImpl(categoryDataSource)

        val keywordDataSource = KeywordRemoteDataSourceImpl(NetworkProvider.keywordService)
        val keywordRepository = KeywordRepositoryImpl(keywordDataSource)
        return SearchViewModel(categoryRepository, keywordRepository) as T
    }
}
