package com.onair.hearit.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class SettingViewModelFactory(
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dataStoreRepository = RepositoryProvider.dataStoreRepository
        val memberRepository = RepositoryProvider.memberRepository
        return SettingViewModel(dataStoreRepository, memberRepository, crashlyticsLogger) as T
    }
}
