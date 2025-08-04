package com.onair.hearit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.RepositoryProvider
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase

@Suppress("UNCHECKED_CAST")
class PlayerViewModelFactory(
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val recentHearitRepository = RepositoryProvider.recentHearitRepository
        val hearitRepository = RepositoryProvider.hearitRepository
        val mediaFileRepository = RepositoryProvider.mediaFileRepository
        val getPlaybackInfoUseCase =
            GetPlaybackInfoUseCase(hearitRepository, mediaFileRepository)

        return PlayerViewModel(
            recentHearitRepository,
            getPlaybackInfoUseCase,
            crashlyticsLogger,
        ) as T
    }
}
