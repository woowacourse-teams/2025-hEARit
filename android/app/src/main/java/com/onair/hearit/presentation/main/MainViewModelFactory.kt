package com.onair.hearit.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.UseCaseProvider

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val getRecentHearitUseCase = UseCaseProvider.getRecentHearitUseCase
        val logoutUseCase = UseCaseProvider.logoutUseCase
        val withdrawUseCase = UseCaseProvider.withdrawUseCase
        return MainViewModel(
            getRecentHearitUseCase,
            logoutUseCase,
            withdrawUseCase,
        ) as T
    }
}
