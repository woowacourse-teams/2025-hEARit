package com.onair.hearit.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.DataSourceProvider
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class SettingViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val preferencesLocalDataSource = DataSourceProvider.preferencesLocalDataSource
        val memberRepository = RepositoryProvider.memberRepository
        return SettingViewModel(preferencesLocalDataSource, memberRepository) as T
    }
}
