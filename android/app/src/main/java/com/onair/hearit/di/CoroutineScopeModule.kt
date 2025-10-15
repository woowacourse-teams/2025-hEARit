package com.onair.hearit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(ServiceComponent::class)
object CoroutineScopeModule {
    @Provides
    @ServiceScope
    fun provideServiceScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}
