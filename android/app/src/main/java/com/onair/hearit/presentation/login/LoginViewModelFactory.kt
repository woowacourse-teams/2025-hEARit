package com.onair.hearit.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.DataSourceProvider
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val preferencesLocalDataSource = DataSourceProvider.preferencesLocalDataSource
        val authRepository = RepositoryProvider.authRepository
        return LoginViewModel(preferencesLocalDataSource, authRepository) as T
    }
}
