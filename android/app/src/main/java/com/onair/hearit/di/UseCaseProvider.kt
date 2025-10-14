package com.onair.hearit.di

import com.onair.hearit.domain.usecase.GetBookmarksUseCase
import com.onair.hearit.domain.usecase.GetExploreHearitUseCase
import com.onair.hearit.domain.usecase.GetHearitUseCase
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase

object UseCaseProvider {
    val getHearitUseCase: GetHearitUseCase by lazy {
        GetHearitUseCase(
            hearitRepository = RepositoryProvider.hearitRepository,
            mediaFileRepository = RepositoryProvider.mediaFileRepository,
        )
    }

    val getPlaybackInfoUseCase: GetPlaybackInfoUseCase by lazy {
        GetPlaybackInfoUseCase(
            hearitRepository = RepositoryProvider.hearitRepository,
            mediaFileRepository = RepositoryProvider.mediaFileRepository,
            recentHearitRepository = RepositoryProvider.recentHearitRepository,
        )
    }

    val getExploreHearitUseCase: GetExploreHearitUseCase by lazy {
        GetExploreHearitUseCase(mediaFileRepository = RepositoryProvider.mediaFileRepository)
    }

    val getBookmarksUseCase: GetBookmarksUseCase by lazy {
        GetBookmarksUseCase(
            bookmarkRepository = RepositoryProvider.bookmarkRepository,
            mediaFileRepository = RepositoryProvider.mediaFileRepository,
        )
    }
}
