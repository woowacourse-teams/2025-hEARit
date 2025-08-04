package com.onair.hearit.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val context: Context,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dataStoreRepository = RepositoryProvider.dataStoreRepository
        val hearitRepository = RepositoryProvider.hearitRepository
        val memberRepository = RepositoryProvider.memberRepository
        return HomeViewModel(
            dataStoreRepository,
            hearitRepository,
            memberRepository,
            crashlyticsLogger,
        ) as T
    }
}
