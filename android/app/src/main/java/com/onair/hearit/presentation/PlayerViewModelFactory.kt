package com.onair.hearit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.repository.RecentHearitRepositoryImpl
import com.onair.hearit.di.DatabaseProvider

@Suppress("UNCHECKED_CAST")
class PlayerViewModelFactory(
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitLocalDataSource =
            HearitLocalDataSourceImpl(DatabaseProvider.hearitDao, crashlyticsLogger)
        val recentHearitRepository =
            RecentHearitRepositoryImpl(hearitLocalDataSource, crashlyticsLogger)

        return PlayerViewModel(recentHearitRepository) as T
    }
}
