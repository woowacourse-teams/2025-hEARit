package com.onair.hearit.di

import com.onair.hearit.data.datasource.AuthRemoteDataSource
import com.onair.hearit.data.datasource.AuthRemoteDataSourceImpl
import com.onair.hearit.data.datasource.BookmarkRemoteDataSource
import com.onair.hearit.data.datasource.BookmarkRemoteDataSourceImpl
import com.onair.hearit.data.datasource.CategoryRemoteDataSource
import com.onair.hearit.data.datasource.CategoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.HearitRemoteDataSource
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.KeywordRemoteDataSource
import com.onair.hearit.data.datasource.KeywordRemoteDataSourceImpl
import com.onair.hearit.data.datasource.MediaFileRemoteDataSource
import com.onair.hearit.data.datasource.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.datasource.MemberRemoteDataSource
import com.onair.hearit.data.datasource.MemberRemoteDataSourceImpl
import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl

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
