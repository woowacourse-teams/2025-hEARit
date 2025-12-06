package com.onair.hearit.di

import com.onair.hearit.data.TokenAuthenticator
import com.onair.hearit.data.api.AuthService
import com.onair.hearit.data.datasource.local.AuthLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TokenAuthenticatorModule {
    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        authLocalDataSource: AuthLocalDataSource,
        @Named("noAuth") authService: AuthService,
    ): TokenAuthenticator = TokenAuthenticator(authLocalDataSource, authService)
}
