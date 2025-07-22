package com.onair.hearit

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class HearitApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
    }
}
