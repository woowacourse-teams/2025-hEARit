package com.onair.hearit.di

import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase

object ServiceProvider {
    val getPlaybackInfoUseCase: GetPlaybackInfoUseCase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        GetPlaybackInfoUseCase(
            hearitRepository = RepositoryProvider.hearitRepository,
            mediaFileRepository = RepositoryProvider.mediaFileRepository,
            recentHearitRepository = RepositoryProvider.recentHearitRepository,
        )
    }
}
