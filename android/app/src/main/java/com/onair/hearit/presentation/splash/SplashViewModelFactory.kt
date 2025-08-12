package com.onair.hearit.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.DataSourceProvider
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class SplashViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepository = RepositoryProvider.authRepository
        val preferencesLocalDataSource = DataSourceProvider.preferencesLocalDataSource
        return SplashViewModel(authRepository, preferencesLocalDataSource) as T
    }
}
