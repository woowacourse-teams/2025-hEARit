package com.onair.hearit.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepository = RepositoryProvider.authRepository
        val dataStoreRepository = RepositoryProvider.dataStoreRepository
        return LoginViewModel(authRepository, dataStoreRepository, crashlyticsLogger) as T
    }
}
