package com.onair.hearit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ServiceScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NormalRetrofit

@Module
@InstallIn(ServiceComponent::class)
object CoroutineScopeModule {
    @Provides
    @ServiceScope
    fun provideServiceScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}
