package com.onair.hearit.di

import com.onair.hearit.di.RepositoryProvider.hearitRepository
import com.onair.hearit.di.RepositoryProvider.mediaFileRepository
import com.onair.hearit.di.RepositoryProvider.recentHearitRepository
import com.onair.hearit.domain.usecase.GetHearitUseCase
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase
import com.onair.hearit.domain.usecase.GetSearchResultUseCase
import com.onair.hearit.domain.usecase.GetShortsHearitUseCase

object UseCaseProvider {
    val getHearitUseCase: GetHearitUseCase by lazy {
        GetHearitUseCase(
            hearitRepository = RepositoryProvider.hearitRepository,
            mediaFileRepository = RepositoryProvider.mediaFileRepository,
        )
    }

    val getPlaybackInfoUseCase: GetPlaybackInfoUseCase by lazy {
        GetPlaybackInfoUseCase(
            hearitRepository = hearitRepository,
            mediaFileRepository = mediaFileRepository,
            recentHearitRepository = recentHearitRepository,
        )
    }

    val getSearchResultUseCase: GetSearchResultUseCase by lazy {
        GetSearchResultUseCase(
            hearitRepository = hearitRepository,
            categoryRepository = RepositoryProvider.categoryRepository,
        )
    }

    val getShortsHearitUseCase: GetShortsHearitUseCase by lazy {
        GetShortsHearitUseCase(mediaFileRepository = mediaFileRepository)
    }
}
