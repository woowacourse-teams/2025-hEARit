package com.onair.hearit.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class SplashViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepository = RepositoryProvider.authRepository
        return SplashViewModel(authRepository) as T
    }
}
