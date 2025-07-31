package com.onair.hearit.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.RecentHearitRepository
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val recentHearitRepository: RecentHearitRepository,
    private val getPlaybackInfoUseCase: GetPlaybackInfoUseCase,
) : ViewModel() {
    private val _playbackInfo = MutableLiveData<PlaybackInfo>()
    val playbackInfo: LiveData<PlaybackInfo> = _playbackInfo

    private val _recentHearit = MutableLiveData<RecentHearit?>()
    val recentHearit: LiveData<RecentHearit?> = _recentHearit

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchRecentHearit()
    }

    fun preparePlayback(hearitId: Long) {
        viewModelScope.launch {
            getPlaybackInfoUseCase(hearitId)
                .onSuccess { _playbackInfo.value = it }
                .onFailure { _toastMessage.value = R.string.main_toast_load_player_hearit_fail }
        }
    }

    private fun fetchRecentHearit() {
        viewModelScope.launch {
            recentHearitRepository
                .getRecentHearit()
                .onSuccess { recent ->
                    _recentHearit.value = recent
                    if (recent != null) {
                        preparePlayback(recent.id)
                    }
                }.onFailure {
                    _toastMessage.value = R.string.main_toast_recent_load_fail
                }
        }
    }
}
