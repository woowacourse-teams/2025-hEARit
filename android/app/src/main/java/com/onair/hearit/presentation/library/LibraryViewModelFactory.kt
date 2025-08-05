package com.onair.hearit.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.RepositoryProvider

@Suppress("UNCHECKED_CAST")
class LibraryViewModelFactory(
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val bookmarkRepository = RepositoryProvider.bookmarkRepository
        val dataStoreRepository = RepositoryProvider.dataStoreRepository
        val memberRepository = RepositoryProvider.memberRepository
        return LibraryViewModel(
            bookmarkRepository,
            dataStoreRepository,
            memberRepository,
            crashlyticsLogger,
        ) as T
    }
}
