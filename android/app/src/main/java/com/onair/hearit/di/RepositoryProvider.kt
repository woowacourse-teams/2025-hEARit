package com.onair.hearit.di

import android.content.Context
import com.onair.hearit.data.repository.AuthRepositoryImpl
import com.onair.hearit.data.repository.BookmarkRepositoryImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
import com.onair.hearit.data.repository.DataStoreRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.KeywordRepositoryImpl
import com.onair.hearit.data.repository.MediaFileRepositoryImpl
import com.onair.hearit.data.repository.MemberRepositoryImpl
import com.onair.hearit.data.repository.RecentHearitRepositoryImpl
import com.onair.hearit.data.repository.RecentKeywordRepositoryImpl
import com.onair.hearit.domain.repository.AuthRepository
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.KeywordRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.domain.repository.MemberRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase

object RepositoryProvider {
    private lateinit var appContext: Context

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authRemoteDataSource = DataSourceProvider.authRemoteDataSource)
    }

    val bookmarkRepository: BookmarkRepository by lazy {
        BookmarkRepositoryImpl(bookmarkDataSource = DataSourceProvider.bookmarkRemoteDataSource)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(categoryDataSource = DataSourceProvider.categoryRemoteDataSource)
    }

    val dataStoreRepository: DataStoreRepository by lazy {
        DataStoreRepositoryImpl(context = appContext)
    }

    val hearitRepository: HearitRepository by lazy {
        HearitRepositoryImpl(hearitRemoteDataSource = DataSourceProvider.hearitRemoteDataSource)
    }

    val keywordRepository: KeywordRepository by lazy {
        KeywordRepositoryImpl(keywordRemoteDataSource = DataSourceProvider.keywordRemoteDataSource)
    }

    val mediaFileRepository: MediaFileRepository by lazy {
        MediaFileRepositoryImpl(mediaFileRemoteDataSource = DataSourceProvider.mediaFileRemoteDataSource)
    }

    val memberRepository: MemberRepository by lazy {
        MemberRepositoryImpl(memberRemoteDataSource = DataSourceProvider.memberRemoteDataSource)
    }

    val recentHearitRepository: RecentHearitRepository by lazy {
        RecentHearitRepositoryImpl(hearitLocalDataSource = DataSourceProvider.hearitLocalDataSource)
    }

    val recentKeywordRepository: RecentKeywordRepository by lazy {
        RecentKeywordRepositoryImpl(hearitLocalDataSource = DataSourceProvider.hearitLocalDataSource)
    }

    val getPlaybackInfoUseCase: GetPlaybackInfoUseCase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        GetPlaybackInfoUseCase(
            hearitRepository = hearitRepository,
            mediaFileRepository = mediaFileRepository,
            recentHearitRepository = recentHearitRepository,
        )
    }
}
