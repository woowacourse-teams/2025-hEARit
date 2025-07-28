package com.onair.hearit.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.AuthRemoteDataSourceImpl
import com.onair.hearit.data.repository.AuthRepositoryImpl
import com.onair.hearit.data.repository.DataStoreRepositoryImpl
import com.onair.hearit.di.NetworkProvider

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRemoteDataSource = AuthRemoteDataSourceImpl(NetworkProvider.authService)
        val authRepository = AuthRepositoryImpl(authRemoteDataSource)
        val dataStoreRepository = DataStoreRepositoryImpl(context)
        return LoginViewModel(authRepository, dataStoreRepository) as T
    }
}
