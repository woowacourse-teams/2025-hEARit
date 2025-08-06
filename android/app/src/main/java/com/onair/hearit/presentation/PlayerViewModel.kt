package com.onair.hearit.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.DataStoreRepository
import com.onair.hearit.domain.repository.RecentHearitRepository
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val dataStoreRepository: DataStoreRepository,
    private val recentHearitRepository: RecentHearitRepository,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModel() {
    private val _recentHearit = MutableLiveData<RecentHearit?>()
    val recentHearit: LiveData<RecentHearit?> = _recentHearit

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchRecentHearit()
    }

    private fun fetchRecentHearit() {
        viewModelScope.launch {
            recentHearitRepository
                .getRecentHearit()
                .onSuccess { recent ->
                    _recentHearit.value = recent
                }.onFailure {
                    _toastMessage.value = R.string.main_toast_recent_load_fail
                }
        }
    }

    fun clearAccessToken() {
        viewModelScope.launch {
            dataStoreRepository
                .clearData()
                .onFailure { _toastMessage.value = R.string.main_toast_clear_token_fail }
        }
    }
}
