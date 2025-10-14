package com.onair.hearit.di

import com.onair.hearit.data.TokenAuthenticator
import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TokenAuthenticatorModule {
    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        preferencesLocalDataSource: PreferencesLocalDataSource,
        authService: AuthService,
    ): TokenAuthenticator =
        TokenAuthenticator(
            { preferencesLocalDataSource },
            { authService },
        )
}
