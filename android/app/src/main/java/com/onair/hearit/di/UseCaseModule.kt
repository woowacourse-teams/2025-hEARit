package com.onair.hearit.di

import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.repository.UserRepository
import com.onair.hearit.domain.usecase.GetBookmarksUseCase
import com.onair.hearit.domain.usecase.GetExploreHearitUseCase
import com.onair.hearit.domain.usecase.GetHearitUseCase
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase
import com.onair.hearit.domain.usecase.GetRecentHearitUseCase
import com.onair.hearit.domain.usecase.InitializeDeviceUuidUseCase
import com.onair.hearit.domain.usecase.auth.KakaoLoginUseCase
import com.onair.hearit.domain.usecase.auth.LogoutUseCase
import com.onair.hearit.domain.usecase.auth.SaveTokenUseCase
import com.onair.hearit.domain.usecase.auth.WithdrawUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideGetHearitUseCase(
        hearitRepository: HearitRepository,
        mediaFileRepository: MediaFileRepository,
    ): GetHearitUseCase =
        GetHearitUseCase(
            hearitRepository = hearitRepository,
            mediaFileRepository = mediaFileRepository,
        )

    @Provides
    @Singleton
    fun provideGetPlaybackInfoUseCase(
        hearitRepository: HearitRepository,
        mediaFileRepository: MediaFileRepository,
        recentHearitRepository: RecentHearitRepository,
    ): GetPlaybackInfoUseCase =
        GetPlaybackInfoUseCase(
            hearitRepository,
            mediaFileRepository,
            recentHearitRepository,
        )

    @Provides
    @Singleton
    fun provideGetExploreHearitUseCase(mediaFileRepository: MediaFileRepository): GetExploreHearitUseCase =
        GetExploreHearitUseCase(mediaFileRepository)

    @Provides
    @Singleton
    fun provideGetBookmarksUseCase(
        bookmarkRepository: BookmarkRepository,
        mediaFileRepository: MediaFileRepository,
    ): GetBookmarksUseCase = GetBookmarksUseCase(bookmarkRepository, mediaFileRepository)

    @Provides
    @Singleton
    fun provideGetRecentHearitUseCase(recentHearitRepository: RecentHearitRepository): GetRecentHearitUseCase =
        GetRecentHearitUseCase(recentHearitRepository)

    @Provides
    @Singleton
    fun provideInitializeDeviceUuidUseCase(userRepository: UserRepository): InitializeDeviceUuidUseCase =
        InitializeDeviceUuidUseCase(userRepository)

    @Provides
    @Singleton
    fun provideKakaoLoginUseCase(authRepository: AuthRepository): KakaoLoginUseCase = KakaoLoginUseCase(authRepository)

    @Provides
    @Singleton
    fun provideSaveTokenUseCase(authRepository: AuthRepository): SaveTokenUseCase = SaveTokenUseCase(authRepository)

    @Provides
    @Singleton
    fun provideLogoutUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository,
    ): LogoutUseCase = LogoutUseCase(authRepository, userRepository)

    @Provides
    @Singleton
    fun provideWithdrawUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository,
    ): WithdrawUseCase = WithdrawUseCase(authRepository, userRepository)
}
