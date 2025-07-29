package com.onair.hearit.presentation.setting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.repository.DataStoreRepositoryImpl

@Suppress("UNCHECKED_CAST")
class SettingViewModelFactory(
    private val context: Context,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dataStoreRepository = DataStoreRepositoryImpl(context, crashlyticsLogger)
        return SettingViewModel(dataStoreRepository) as T
    }
}
