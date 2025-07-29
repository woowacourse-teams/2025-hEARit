package com.onair.hearit.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.datasource.BookmarkRemoteDataSourceImpl
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.KeywordRemoteDataSourceImpl
import com.onair.hearit.data.datasource.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.repository.BookmarkRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.KeywordRepositoryImpl
import com.onair.hearit.data.repository.MediaFileRepositoryImpl
import com.onair.hearit.data.repository.RecentHearitRepositoryImpl
import com.onair.hearit.di.DatabaseProvider
import com.onair.hearit.di.NetworkProvider
import com.onair.hearit.domain.usecase.GetHearitUseCase

@Suppress("UNCHECKED_CAST")
class PlayerDetailViewModelFactory(
    private val hearitId: Long,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource, crashlyticsLogger)

        val hearitLocalDataSource =
            HearitLocalDataSourceImpl(DatabaseProvider.hearitDao, crashlyticsLogger)
        val recentHearitRepository =
            RecentHearitRepositoryImpl(hearitLocalDataSource, crashlyticsLogger)

        val mediaFileRemoteDataSource =
            MediaFileRemoteDataSourceImpl(NetworkProvider.mediaFileService)
        val mediaFileRepository =
            MediaFileRepositoryImpl(mediaFileRemoteDataSource, crashlyticsLogger)

        val keywordRemoteDataSource = KeywordRemoteDataSourceImpl(NetworkProvider.keywordService)
        val keywordRepository = KeywordRepositoryImpl(keywordRemoteDataSource, crashlyticsLogger)

        val getHearitUseCase = GetHearitUseCase(hearitRepository, mediaFileRepository)

        val bookmarkRemoteDataSource = BookmarkRemoteDataSourceImpl(NetworkProvider.bookmarkService)
        val bookmarkRepository = BookmarkRepositoryImpl(bookmarkRemoteDataSource, crashlyticsLogger)

        return PlayerDetailViewModel(
            hearitId,
            recentHearitRepository,
            getHearitUseCase,
            bookmarkRepository,
            keywordRepository,
        ) as T
    }
}
