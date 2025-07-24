package com.onair.hearit.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.domain.model.PlaybackInfo
import com.onair.hearit.domain.usecase.GetPlaybackInfoUseCase
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val getPlaybackInfoUseCase: GetPlaybackInfoUseCase,
) : ViewModel() {
    private val _playbackInfo = MutableLiveData<PlaybackInfo>()
    val playbackInfo: LiveData<PlaybackInfo> = _playbackInfo

    fun preparePlayback(hearitId: Long) {
        viewModelScope.launch {
            val result = getPlaybackInfoUseCase(hearitId)
            if (result.isSuccess) {
                _playbackInfo.value = result.getOrNull()
            }
        }
    }
}
