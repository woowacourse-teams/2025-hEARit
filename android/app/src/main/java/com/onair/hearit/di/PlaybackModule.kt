package com.onair.hearit.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.onair.hearit.domain.repository.PlayingHistoryRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.usecase.GetBookmarksUseCase
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase
import com.onair.hearit.service.LibraryPlaybackHandler
import com.onair.hearit.service.PlaybackMediaItemManager
import com.onair.hearit.service.PlaybackStateSaver
import com.onair.hearit.service.RecentPlaybackHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ServiceComponent::class)
object PlaybackModule {
    @Provides
    @ServiceScoped
    fun provideExoPlayer(
        @ApplicationContext context: Context,
    ): ExoPlayer {
        val audioAttributes =
            AudioAttributes
                .Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()

        return ExoPlayer
            .Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .build()
            .apply {
                playWhenReady = false
                setHandleAudioBecomingNoisy(true)
            }
    }

    @Provides
    @ServiceScoped
    fun providePlayer(exoPlayer: ExoPlayer): Player = exoPlayer

    @Provides
    @ServiceScoped
    fun providePlaybackStateSaver(
        player: Player,
        @ServiceScope serviceScope: CoroutineScope,
        recentHearitRepository: RecentHearitRepository,
        playingHistoryRepository: PlayingHistoryRepository,
    ): PlaybackStateSaver =
        PlaybackStateSaver(
            player,
            serviceScope,
            recentHearitRepository,
            playingHistoryRepository,
        )

    @Provides
    @ServiceScoped
    @OptIn(UnstableApi::class)
    fun providePlaybackMediaItemManager(getPlaybackInfoUseCase: GetPlaybackInfoUseCase): PlaybackMediaItemManager =
        PlaybackMediaItemManager(getPlaybackInfoUseCase)

    @Provides
    @ServiceScoped
    @OptIn(UnstableApi::class)
    fun provideLibraryPlaybackHandler(
        getBookmarksUseCase: GetBookmarksUseCase,
        playbackMediaItemManager: PlaybackMediaItemManager,
    ): LibraryPlaybackHandler =
        LibraryPlaybackHandler(
            getBookmarksUseCase,
            playbackMediaItemManager,
        )

    @Provides
    @ServiceScoped
    @OptIn(UnstableApi::class)
    fun provideRecentPlaybackHandler(
        recentHearitRepository: RecentHearitRepository,
        getPlaybackInfoUseCase: GetPlaybackInfoUseCase,
        playbackMediaItemManager: PlaybackMediaItemManager,
    ): RecentPlaybackHandler =
        RecentPlaybackHandler(
            recentHearitRepository,
            getPlaybackInfoUseCase,
            playbackMediaItemManager,
        )
}
