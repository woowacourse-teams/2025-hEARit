package com.onair.hearit.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.di.UseCaseProvider

@Suppress("UNCHECKED_CAST")
class ExploreViewModelFactory(
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitRepository = RepositoryProvider.hearitRepository
        val bookmarkRepository = RepositoryProvider.bookmarkRepository
        val exploreDataStoreRepository = RepositoryProvider.exploreDataStoreRepository
        val getShortsHearitUseCase = UseCaseProvider.getShortsHearitUseCase

        return ExploreViewModel(
            hearitRepository,
            bookmarkRepository,
            exploreDataStoreRepository,
            getShortsHearitUseCase,
            crashlyticsLogger,
        ) as T
    }
}
