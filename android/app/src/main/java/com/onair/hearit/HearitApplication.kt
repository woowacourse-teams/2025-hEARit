package com.onair.hearit

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.onair.hearit.di.DatabaseProvider

class HearitApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        DatabaseProvider.init(this)
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
    }
}
