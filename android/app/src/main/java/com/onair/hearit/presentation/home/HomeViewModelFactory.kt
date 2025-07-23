package com.onair.hearit.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.CategoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.RecentHearitRepositoryImpl
import com.onair.hearit.di.DatabaseProvider
import com.onair.hearit.di.NetworkProvider

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val categoryDataSource = CategoryRemoteDataSourceImpl(NetworkProvider.categoryService)
        val categoryRepository = CategoryRepositoryImpl(categoryDataSource)
        val hearitLocalDataSource = HearitLocalDataSourceImpl(DatabaseProvider.hearitDao)
        val recentHearitRepository = RecentHearitRepositoryImpl(hearitLocalDataSource)
        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource)
        return HomeViewModel(categoryRepository, hearitRepository, recentHearitRepository) as T
    }
}
