package com.onair.hearit.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.DataSourceProvider
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepository = RepositoryProvider.authRepository
        val preferencesLocalDataSource = DataSourceProvider.preferencesLocalDataSource
        val recentHearitRepository = RepositoryProvider.recentHearitRepository
        return MainViewModel(
            authRepository,
            preferencesLocalDataSource,
            recentHearitRepository,
        ) as T
    }
}
