package com.onair.hearit.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSource
import com.onair.hearit.data.datasource.local.PreferencesLocalDataSourceImpl
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
import com.onair.hearit.data.datasource.remote.MemberRemoteDataSource
import com.onair.hearit.data.datasource.remote.MemberRemoteDataSourceImpl

object DataSourceProvider {
    private lateinit var dataStore: DataStore<Preferences>
    private val errorHandler = ErrorResponseHandler()

    fun init(context: Context) {
        val appContext = context.applicationContext
        dataStore =
            PreferenceDataStoreFactory.create { appContext.preferencesDataStoreFile("user_prefs") }
    }

    val authRemoteDataSource: AuthRemoteDataSource by lazy {
        AuthRemoteDataSourceImpl(
            authService = NetworkProvider.authService,
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

    val memberRemoteDataSource: MemberRemoteDataSource by lazy {
        MemberRemoteDataSourceImpl(
            memberService = NetworkProvider.memberService,
            errorResponseHandler = errorHandler,
        )
    }

    val hearitLocalDataSource: HearitLocalDataSource by lazy {
        HearitLocalDataSourceImpl(DatabaseProvider.hearitDao)
    }

    val preferencesLocalDataSource: PreferencesLocalDataSource by lazy {
        check(::dataStore.isInitialized) { "DataSourceProvider.init() 먼저 호출 필요" }
        PreferencesLocalDataSourceImpl(dataStore)
    }
}
