package com.onair.hearit.di

import com.onair.hearit.domain.usecase.GetBookmarksUseCase
import com.onair.hearit.domain.usecase.GetExploreHearitUseCase
import com.onair.hearit.domain.usecase.GetHearitUseCase
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase
import com.onair.hearit.domain.usecase.InitializeDeviceUuidUseCase
import com.onair.hearit.domain.usecase.auth.GetRecentHearitUseCase
import com.onair.hearit.domain.usecase.auth.LogoutUseCase
import com.onair.hearit.domain.usecase.auth.WithdrawUseCase

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

    val logoutUseCase: LogoutUseCase by lazy {
        LogoutUseCase(
            authRepository = RepositoryProvider.authRepository,
            userRepository = RepositoryProvider.userRepository,
        )
    }

    val withdrawUseCase: WithdrawUseCase by lazy {
        WithdrawUseCase(
            authRepository = RepositoryProvider.authRepository,
            userRepository = RepositoryProvider.userRepository,
        )
    }

    val getRecentHearitUseCase: GetRecentHearitUseCase by lazy {
        GetRecentHearitUseCase(RepositoryProvider.recentHearitRepository)
    }

    val initializeDeviceUuidUseCase: InitializeDeviceUuidUseCase by lazy {
        InitializeDeviceUuidUseCase(RepositoryProvider.userRepository)
    }
}
