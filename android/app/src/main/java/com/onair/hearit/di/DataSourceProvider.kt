package com.onair.hearit.di

import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.datasource.remote.AuthRemoteDataSource
import com.onair.hearit.data.datasource.remote.AuthRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSource
import com.onair.hearit.data.datasource.remote.BookmarkRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.CategoryRemoteDataSource
import com.onair.hearit.data.datasource.remote.CategoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSource
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.KeywordRemoteDataSource
import com.onair.hearit.data.datasource.remote.KeywordRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.MediaFileRemoteDataSource
import com.onair.hearit.data.datasource.remote.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.datasource.remote.MemberRemoteDataSource
import com.onair.hearit.data.datasource.remote.MemberRemoteDataSourceImpl

object DataSourceProvider {
    private val errorHandler = ErrorResponseHandler()

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

    val keywordRemoteDataSource: KeywordRemoteDataSource by lazy {
        KeywordRemoteDataSourceImpl(
            keywordService = NetworkProvider.keywordService,
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
}
