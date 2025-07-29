package com.onair.hearit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.MediaFileRepositoryImpl
import com.onair.hearit.data.repository.RecentHearitRepositoryImpl
import com.onair.hearit.di.DatabaseProvider
import com.onair.hearit.di.NetworkProvider
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase

@Suppress("UNCHECKED_CAST")
class PlayerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitLocalDataSource = HearitLocalDataSourceImpl(DatabaseProvider.hearitDao)
        val recentHearitRepository = RecentHearitRepositoryImpl(hearitLocalDataSource)

        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource)

        val mediaFileRemoteDataSource =
            MediaFileRemoteDataSourceImpl(NetworkProvider.mediaFileService)
        val mediaFileRepository = MediaFileRepositoryImpl(mediaFileRemoteDataSource)

        val getPlaybackInfoUseCase =
            GetPlaybackInfoUseCase(
                hearitRepository = hearitRepository,
                mediaFileRepository = mediaFileRepository,
            )

        return PlayerViewModel(recentHearitRepository, getPlaybackInfoUseCase) as T
    }
}
