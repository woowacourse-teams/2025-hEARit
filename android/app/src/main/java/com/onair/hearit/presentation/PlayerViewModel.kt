package com.onair.hearit.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.RecentHearitRepository
import kotlinx.coroutines.launch

class PlayerViewModel(
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

    fun savePlaybackPosition(
        position: Long,
        duration: Long,
        hearitId: Long,
    ) {
        if (duration <= 0) return
        val isFinished = position >= duration - 1000

        viewModelScope.launch {
            recentHearitRepository
                .updateRecentHearitPosition(
                    hearitId = hearitId,
                    position = if (isFinished) 0L else position,
                ).onFailure {
                    _toastMessage.value = R.string.main_toast_player_last_position_save_fail
                }
        }
    }
}
