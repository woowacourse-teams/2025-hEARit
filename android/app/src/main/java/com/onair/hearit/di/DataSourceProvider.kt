package com.onair.hearit.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.local.AuthLocalDataSource
import com.onair.hearit.data.datasource.local.AuthLocalDataSourceImpl
import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.datasource.local.UserLocalDataSource
import com.onair.hearit.data.datasource.local.UserLocalDataSourceImpl
import com.onair.hearit.data.datasource.remote.AuthRemoteDataSource
import com.onair.hearit.data.datasource.remote.AuthRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSource
import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.CategoryRemoteDataSource
import com.onair.hearit.data.datasource.remote.CategoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSource
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.MediaFileRemoteDataSource
import com.onair.hearit.data.datasource.remote.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.PlayingHistoryRemoteDataSource
import com.onair.hearit.data.datasource.remote.PlayingHistoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.RecommendationRemoteDataSource
import com.onair.hearit.data.datasource.remote.RecommendationRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.UserRemoteDataSource
import com.onair.hearit.data.datasource.remote.UserRemoteDataSourceImpl

object DataSourceProvider {
    private lateinit var authDataStore: DataStore<Preferences>
    private lateinit var userDataStore: DataStore<Preferences>
    private val errorHandler = ErrorResponseHandler()

    fun init(context: Context) {
        val appContext = context.applicationContext
        authDataStore =
            PreferenceDataStoreFactory.create {
                appContext.preferencesDataStoreFile("auth_prefs")
            }
        userDataStore =
            PreferenceDataStoreFactory.create {
                appContext.preferencesDataStoreFile("user_prefs")
            }
    }

    val authRemoteDataSource: AuthRemoteDataSource by lazy {
        AuthRemoteDataSourceImpl(
            authService = NetworkProvider.authServiceNoAuth,
            errorResponseHandler = errorHandler,
        )
    }

    val bookmarkRemoteDataSource: BookmarkRemoteDataSource by lazy {
        BookmarkRemoteDataSourceImpl(
            bookmarkService = NetworkProvider.bookmarkService,
            errorResponseHandler = errorHandler,
        )
    }

    val categoryRemoteDataSource: CategoryRemoteDataSource by lazy {
        CategoryRemoteDataSourceImpl(
            categoryService = NetworkProvider.categoryService,
            errorResponseHandler = errorHandler,
        )
    }

    val hearitRemoteDataSource: HearitRemoteDataSource by lazy {
        HearitRemoteDataSourceImpl(
            hearitService = NetworkProvider.hearitService,
            errorResponseHandler = errorHandler,
        )
    }

    val mediaFileRemoteDataSource: MediaFileRemoteDataSource by lazy {
        MediaFileRemoteDataSourceImpl(
            mediaFileService = NetworkProvider.mediaFileService,
            errorResponseHandler = errorHandler,
        )
    }

    val userRemoteDataSource: UserRemoteDataSource by lazy {
        UserRemoteDataSourceImpl(
            memberService = NetworkProvider.memberService,
            errorResponseHandler = errorHandler,
        )
    }

    val playingHistoryRemoteDataSource: PlayingHistoryRemoteDataSource by lazy {
        PlayingHistoryRemoteDataSourceImpl(
            playingHistoryService = NetworkProvider.playingHistoryService,
            errorResponseHandler = errorHandler,
        )
    }

    val recommendationRemoteDataSource: RecommendationRemoteDataSource by lazy {
        RecommendationRemoteDataSourceImpl(
            recommendationService = NetworkProvider.recommendationService,
            errorResponseHandler = errorHandler,
        )
    }

    // Local
    val hearitLocalDataSource: HearitLocalDataSource by lazy {
        HearitLocalDataSourceImpl(DatabaseProvider.hearitDao)
    }

    val authLocalDataSource: AuthLocalDataSource by lazy {
        check(::authDataStore.isInitialized) { "DataSourceProvider.init() 먼저 호출 필요" }
        AuthLocalDataSourceImpl(authDataStore)
    }

    val userLocalDataSource: UserLocalDataSource by lazy {
        check(::userDataStore.isInitialized) { "DataSourceProvider.init() 먼저 호출 필요" }
        UserLocalDataSourceImpl(userDataStore)
    }
}
