package com.onair.hearit.presentation.splash

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class SplashViewModel(
    private val dataStoreRepository: DataStoreRepository,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModel() {
    private val _checkToken: MutableLiveData<Boolean> = MutableLiveData()
    val checkToken: LiveData<Boolean> = _checkToken

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    fun checkAccessToken() {
        viewModelScope.launch {
            dataStoreRepository
                .getAccessToken()
                .onSuccess { token ->
                    Log.d("meeple_log", token)
                    _checkToken.value = true
                }.onFailure {
                    _checkToken.value = false
                }
        }
    }
}
