package com.onair.hearit

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.di.TokenInterceptorProvider
import com.onair.hearit.domain.usecase.InitializeDeviceUuidUseCase
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
    lateinit var initializeDeviceUuidUseCase: InitializeDeviceUuidUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)

        initUuid()
        setAppVersion()
        initialTimber()
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }

    private fun initUuid() {
        appScope.launch {
            initializeDeviceUuidUseCase()
                .onSuccess { uuid ->
                    TokenInterceptorProvider.setDeviceUuid(uuid)
                }.onFailure { throwable ->
                    Timber.e(throwable, "Failed to initialize UUID")
                }
        }
    }

    private fun setAppVersion() {
        val versionName =
            try {
                packageManager.getPackageInfo(packageName, 0).versionName
            } catch (e: Exception) {
                "unknown"
            }

        TokenInterceptorProvider.setAppVersion(versionName)
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
        Timber.plant(ReleaseTree())
    }

    class ReleaseTree : Timber.Tree() {
        override fun log(
            priority: Int,
            tag: String?,
            message: String,
            t: Throwable?,
        ) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            if (t != null) {
                if (priority == Log.ERROR) {
                    CrashlyticsProvider.get().recordException(t)
                } else if (priority == Log.WARN) {
                    val warningMessage = t.message ?: ERROR_UNKNOWN_MESSAGE
                    CrashlyticsProvider.get().recordException(
                        RuntimeException(warningMessage, t),
                    )
                }
            }
        }

        override fun isLoggable(
            tag: String?,
            priority: Int,
        ): Boolean = priority >= Log.INFO
    }

    companion object {
        private const val TIMBER_LOG_PREFIX = "hEARit_LOG"
        private const val ERROR_UNKNOWN_MESSAGE = "알 수 없는 Error"
    }
}
