package com.onair.hearit

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.di.DatabaseProvider

class HearitApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        DatabaseProvider.init(this)
        AnalyticsProvider.init(this)
        CrashlyticsProvider.init()
    }
}
