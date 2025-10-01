package com.onair.hearit.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.di.UseCaseProvider

@Suppress("UNCHECKED_CAST")
class ExploreViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitRepository = RepositoryProvider.hearitRepository
        val exploreDataStoreRepository = RepositoryProvider.exploreDataStoreRepository
        val getShortsHearitUseCase = UseCaseProvider.getExploreHearitUseCase

        return ExploreViewModel(
            hearitRepository,
            exploreDataStoreRepository,
            getShortsHearitUseCase,
        ) as T
    }
}
