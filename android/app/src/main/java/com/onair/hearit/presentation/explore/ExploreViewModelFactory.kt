package com.onair.hearit.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.HearitRemoteDataSourceImpl
import com.onair.hearit.data.HearitRepositoryImpl
import com.onair.hearit.data.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.MediaFileRepositoryImpl
import com.onair.hearit.di.NetworkProvider

@Suppress("UNCHECKED_CAST")
class ExploreViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource)
        val mediaFileRemoteDataSource =
            MediaFileRemoteDataSourceImpl(NetworkProvider.mediaFileService)
        val mediaFileRepository = MediaFileRepositoryImpl(mediaFileRemoteDataSource)
        return ExploreViewModel(hearitRepository, mediaFileRepository) as T
    }
}
