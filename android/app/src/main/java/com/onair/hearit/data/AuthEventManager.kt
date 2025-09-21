package com.onair.hearit.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthEventManager {
    private val _logoutEvent = MutableSharedFlow<Unit>(replay = 1)
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    @Volatile
    private var isValidSession = true

    fun sendLogoutEvent() {
        isValidSession = false
        _logoutEvent.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onLoginSuccess() {
        isValidSession = true
        _logoutEvent.resetReplayCache()
    }

    fun isValidSession() = isValidSession
}
