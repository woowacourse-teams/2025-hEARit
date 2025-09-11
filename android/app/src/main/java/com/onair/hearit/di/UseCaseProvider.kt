package com.onair.hearit.di

import com.onair.hearit.domain.usecase.GetBookmarksUseCase
import com.onair.hearit.domain.usecase.GetHearitUseCase
import com.onair.hearit.domain.usecase.GetNextBookmarkUseCase
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

    val getNextBookmarkUseCase: GetNextBookmarkUseCase by lazy {
        GetNextBookmarkUseCase(
            bookmarkRepository = RepositoryProvider.bookmarkRepository,
            mediaFileRepository = RepositoryProvider.mediaFileRepository,
        )
    }

    val getBookmarksUseCase: GetBookmarksUseCase by lazy {
        GetBookmarksUseCase(
            bookmarkRepository = RepositoryProvider.bookmarkRepository,
            mediaFileRepository = RepositoryProvider.mediaFileRepository,
        )
    }
}
