package com.onair.hearit.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthHeaderProvider @Inject constructor() {
    @Volatile
    private var accessToken: String? = null

    @Volatile
    private var deviceUuid: String? = null

    @Volatile
    private var appVersion: String? = null

    fun getAccessToken(): String? = accessToken

    fun getDeviceUuid(): String? = deviceUuid

    fun getAppVersion(): String? = appVersion

    fun updateAccessToken(token: String?) {
        accessToken = token
    }

    fun updateDeviceUuid(uuid: String?) {
        deviceUuid = uuid
    }

    fun updateAppVersion(version: String?) {
        appVersion = version
    }
}
