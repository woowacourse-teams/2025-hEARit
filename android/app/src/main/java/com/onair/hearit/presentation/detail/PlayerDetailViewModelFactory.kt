package com.onair.hearit.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.domain.usecase.GetHearitUseCase

@Suppress("UNCHECKED_CAST")
class PlayerDetailViewModelFactory(
    private val hearitId: Long,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitRepository = RepositoryProvider.hearitRepository
        val recentHearitRepository = RepositoryProvider.recentHearitRepository
        val mediaFileRepository = RepositoryProvider.mediaFileRepository
        val bookmarkRepository = RepositoryProvider.bookmarkRepository
        val getHearitUseCase = GetHearitUseCase(hearitRepository, mediaFileRepository)

        return PlayerDetailViewModel(
            hearitId,
            recentHearitRepository,
            getHearitUseCase,
            bookmarkRepository,
            crashlyticsLogger,
        ) as T
    }
}
