package com.onair.hearit.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.BookmarkRemoteDataSourceImpl
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.repository.BookmarkRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.MediaFileRepositoryImpl
import com.onair.hearit.di.NetworkProvider
import com.onair.hearit.domain.usecase.GetHearitUseCase

@Suppress("UNCHECKED_CAST")
class PlayerDetailViewModelFactory(
    private val hearitId: Long,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource)

        val mediaFileRemoteDataSource =
            MediaFileRemoteDataSourceImpl(NetworkProvider.mediaFileService)
        val mediaFileRepository = MediaFileRepositoryImpl(mediaFileRemoteDataSource)

        val getHearitUseCase = GetHearitUseCase(hearitRepository, mediaFileRepository)

        val bookmarkRemoteDataSource = BookmarkRemoteDataSourceImpl(NetworkProvider.bookmarkService)
        val bookmarkRepository = BookmarkRepositoryImpl(bookmarkRemoteDataSource)

        return PlayerDetailViewModel(
            hearitId,
            getHearitUseCase,
            bookmarkRepository,
        ) as T
    }
}
