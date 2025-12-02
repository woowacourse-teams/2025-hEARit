package com.onair.hearit.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.UseCaseProvider

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val kakaoLoginUseCase = UseCaseProvider.kakaoLoginUseCase
        val saveTokenUseCase = UseCaseProvider.saveTokenUseCase
        return LoginViewModel(kakaoLoginUseCase, saveTokenUseCase) as T
    }
}
