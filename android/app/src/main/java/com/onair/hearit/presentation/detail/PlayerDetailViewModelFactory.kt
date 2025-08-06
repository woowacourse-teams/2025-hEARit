package com.onair.hearit.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.di.UseCaseProvider

@Suppress("UNCHECKED_CAST")
class PlayerDetailViewModelFactory(
    private val hearitId: Long,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val bookmarkRepository = RepositoryProvider.bookmarkRepository
        val recentHearitRepository = RepositoryProvider.recentHearitRepository
        val getHearitUseCase = UseCaseProvider.getHearitUseCase

        return PlayerDetailViewModel(
            hearitId,
            recentHearitRepository,
            getHearitUseCase,
            bookmarkRepository,
            crashlyticsLogger,
        ) as T
    }
}
