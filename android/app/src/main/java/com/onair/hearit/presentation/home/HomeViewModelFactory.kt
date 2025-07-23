package com.onair.hearit.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.CategoryDataSourceImpl
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.di.NetworkProvider

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val categoryDataSource = CategoryDataSourceImpl(NetworkProvider.categoryService)
        val categoryRepository = CategoryRepositoryImpl(categoryDataSource)
        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource)
        return HomeViewModel(categoryRepository, hearitRepository) as T
    }
}
