package com.onair.hearit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.DataSourceProvider
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepository = RepositoryProvider.authRepository
        val recentHearitRepository = RepositoryProvider.recentHearitRepository
        val preferencesLocalDataSource = DataSourceProvider.preferencesLocalDataSource
        return MainViewModel(
            authRepository,
            preferencesLocalDataSource,
            recentHearitRepository,
        ) as T
    }
}
