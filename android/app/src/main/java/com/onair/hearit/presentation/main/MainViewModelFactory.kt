package com.onair.hearit.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepository = RepositoryProvider.authRepository
        val recentHearitRepository = RepositoryProvider.recentHearitRepository
        val userRepository = RepositoryProvider.userRepository
        return MainViewModel(
            authRepository,
            recentHearitRepository,
            userRepository,
        ) as T
    }
}
