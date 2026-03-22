package com.onair.hearit.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthPreferencesDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserPreferencesDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ExplorePreferencesDataStore
