package com.onair.hearit.di

import com.onair.hearit.domain.usecase.GetHearitUseCase
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase
import com.onair.hearit.domain.usecase.GetSearchResultUseCase
import com.onair.hearit.domain.usecase.GetShortsHearitUseCase

object UseCaseProvider {
    val getHearitUseCase: GetHearitUseCase by lazy {
        GetHearitUseCase(
            dataStoreRepository = RepositoryProvider.dataStoreRepository,
            hearitRepository = RepositoryProvider.hearitRepository,
            mediaFileRepository = RepositoryProvider.mediaFileRepository,
        )
    }

    val getPlaybackInfoUseCase: GetPlaybackInfoUseCase by lazy {
        GetPlaybackInfoUseCase(
            dataStoreRepository = RepositoryProvider.dataStoreRepository,
            hearitRepository = RepositoryProvider.hearitRepository,
            mediaFileRepository = RepositoryProvider.mediaFileRepository,
            recentHearitRepository = RepositoryProvider.recentHearitRepository,
        )
    }

    val getSearchResultUseCase: GetSearchResultUseCase by lazy {
        GetSearchResultUseCase(
            hearitRepository = RepositoryProvider.hearitRepository,
            categoryRepository = RepositoryProvider.categoryRepository,
        )
    }

    val getShortsHearitUseCase: GetShortsHearitUseCase by lazy {
        GetShortsHearitUseCase(mediaFileRepository = RepositoryProvider.mediaFileRepository)
    }
}
