package com.onair.hearit

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.MediaFileRemoteDataSourceImpl
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.MediaFileRepositoryImpl
import com.onair.hearit.data.repository.RecentHearitRepositoryImpl
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.di.DatabaseProvider
import com.onair.hearit.di.NetworkProvider
import com.onair.hearit.di.ServiceProvider

class HearitApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        DatabaseProvider.init(this)
        AnalyticsProvider.init(this)
        CrashlyticsProvider.init()

        val crashlytics = CrashlyticsProvider.get()

        val localDS =
            HearitLocalDataSourceImpl(
                hearitDao = DatabaseProvider.hearitDao,
                crashlyticsLogger = crashlytics,
            )

        val hearitRemoteDS =
            HearitRemoteDataSourceImpl(
                hearitService = NetworkProvider.hearitService,
            )
        val mediaFileRemoteDS =
            MediaFileRemoteDataSourceImpl(
                mediaFileService = NetworkProvider.mediaFileService,
            )

        val recentRepo =
            RecentHearitRepositoryImpl(
                hearitLocalDataSource = localDS,
                crashlyticsLogger = crashlytics,
            )
        val hearitRepo =
            HearitRepositoryImpl(
                hearitRemoteDataSource = hearitRemoteDS,
                crashlyticsLogger = crashlytics,
            )
        val mediaRepo =
            MediaFileRepositoryImpl(
                mediaFileRemoteDataSource = mediaFileRemoteDS,
                crashlyticsLogger = crashlytics,
            )

        ServiceProvider.init(
            app = this,
            hearitRepo = hearitRepo,
            mediaRepo = mediaRepo,
            recentRepo = recentRepo,
        )
    }
}
