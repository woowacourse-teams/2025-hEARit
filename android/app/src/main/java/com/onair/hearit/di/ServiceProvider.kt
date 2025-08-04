package com.onair.hearit.di

import android.app.Application
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase

object ServiceProvider {
    private lateinit var application: Application

    private lateinit var hearitRepository: HearitRepository
    private lateinit var mediaFileRepository: MediaFileRepository
    private lateinit var recentHearitRepository: RecentHearitRepository

    fun init(
        app: Application,
        hearitRepo: HearitRepository,
        mediaRepo: MediaFileRepository,
        recentRepo: RecentHearitRepository,
    ) {
        application = app
        hearitRepository = hearitRepo
        mediaFileRepository = mediaRepo
        recentHearitRepository = recentRepo
    }

    val getPlaybackInfoUseCase: GetPlaybackInfoUseCase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        GetPlaybackInfoUseCase(
            hearitRepository = hearitRepository,
            mediaFileRepository = mediaFileRepository,
            recentHearitRepository = recentHearitRepository,
        )
    }

    fun recentHearitRepository(): RecentHearitRepository = recentHearitRepository
}
