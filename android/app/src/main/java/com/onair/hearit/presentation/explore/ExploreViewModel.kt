package com.onair.hearit.presentation.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.HearitShorts
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.MediaFileRepository
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val hearitRepository: HearitRepository,
    private val mediaFileRepository: MediaFileRepository,
) : ViewModel() {
    private val _shortsHearits: MutableLiveData<List<HearitShorts>> = MutableLiveData()
    val shortsHearits: LiveData<List<HearitShorts>> = _shortsHearits

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            hearitRepository
                .getRandomHearits()
                .onSuccess { randomItems ->
                    val shortsItem: List<HearitShorts> =
                        randomItems.mapNotNull { item ->
                            val result = mediaFileRepository.getShortsHearitItem(item)
                            if (result.isFailure) {
                                _toastMessage.value = R.string.explore_toast_shorts_load_fail
                            }
                            result.getOrNull()
                        }

                    _shortsHearits.value = shortsItem
                }.onFailure {
                    _toastMessage.value = R.string.explore_toast_random_hearits_load_fail
                }
        }
    }
}
