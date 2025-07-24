package com.onair.hearit.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.BookmarkRemoteDataSourceImpl
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.repository.BookmarkRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.MediaFileRepositoryImpl
import com.onair.hearit.di.NetworkProvider
import com.onair.hearit.domain.usecase.GetShortsHearitUseCase

@Suppress("UNCHECKED_CAST")
class ExploreViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource)

        val bookmarkRemoteDataSource = BookmarkRemoteDataSourceImpl(NetworkProvider.bookmarkService)
        val bookmarkRepository = BookmarkRepositoryImpl(bookmarkRemoteDataSource)

        val mediaFileRemoteDataSource =
            MediaFileRemoteDataSourceImpl(NetworkProvider.mediaFileService)
        val mediaFileRepository = MediaFileRepositoryImpl(mediaFileRemoteDataSource)

        val getShortsHearitUseCase = GetShortsHearitUseCase(mediaFileRepository)

        return ExploreViewModel(
            hearitRepository = hearitRepository,
            bookmarkRepository = bookmarkRepository,
            getShortsHearitUseCase = getShortsHearitUseCase,
        ) as T
    }
}
