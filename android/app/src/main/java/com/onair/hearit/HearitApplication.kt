package com.onair.hearit

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.di.TokenInterceptorProvider
import com.onair.hearit.presentation.UserIdManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class HearitApplication : Application() {
    @Inject
    lateinit var releaseTree: ReleaseTree
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        AnalyticsProvider.init(this)
        initUuid()
        initialTimber()
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }

    private fun initUuid() {
        appScope.launch {
            val uuid = UserIdManager.getOrCreateUserId(applicationContext)
            TokenInterceptorProvider.setDeviceUuid(uuid)
        }
    }

    private fun initialTimber() {
        if (BuildConfig.DEBUG) {
            plantDebugTimberTree()
        } else {
            plantReleaseTimberTree()
        }
    }

    private fun plantDebugTimberTree() {
        Timber.plant(
            object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String =
                    "$TIMBER_LOG_PREFIX ${element.fileName}: ${element.lineNumber}"
            },
        )
    }

    private fun plantReleaseTimberTree() {
        Timber.plant(releaseTree)
    }

    class ReleaseTree @Inject constructor(
        private val crashlyticsLogger: CrashlyticsLogger,
    ) : Timber.Tree() {
        override fun log(
            priority: Int,
            tag: String?,
            message: String,
            t: Throwable?,
        ) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) return

            val throwableToLog = t ?: if (priority >= Log.WARN) RuntimeException(message) else null

            throwableToLog?.let {
                crashlyticsLogger.recordException(it)
            }
        }

        override fun isLoggable(
            tag: String?,
            priority: Int,
        ): Boolean = priority >= Log.INFO
    }

    companion object {
        private const val TIMBER_LOG_PREFIX = "hEARit_LOG"
    }
}
