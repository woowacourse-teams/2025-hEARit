package com.onair.hearit.presentation.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.HearitShortsItem
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val hearitRepository: HearitRepository,
    private val mediaFileRepository: MediaFileRepository,
) : ViewModel() {
    private val _shortsHearits: MutableLiveData<List<HearitShortsItem>> = MutableLiveData()
    val shortsHearits: LiveData<List<HearitShortsItem>> = _shortsHearits

    private val _toastMessage = MutableLiveData<Int>()
    val toastMessage: LiveData<Int> get() = _toastMessage

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            hearitRepository
                .getRandomHearits()
                .onSuccess { randomItems ->
                    val shortsItem: List<HearitShortsItem> =
                        randomItems.mapNotNull { item ->
                            val result = mediaFileRepository.getShortsHearitItem(item)
                            if (result.isFailure) {
                                _toastMessage.value = R.string.explore_toast_shorts_load_fail
                            }
                            result.getOrNull()
                        }

                    _shortsHearits.value = shortsItem
                }.onFailure {
                    _toastMessage.value = R.string.explore_toast_shorts_load_fail
                }
        }
    }
}
