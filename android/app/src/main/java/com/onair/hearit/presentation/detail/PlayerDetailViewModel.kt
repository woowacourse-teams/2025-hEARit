package com.onair.hearit.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.Hearit
import com.onair.hearit.domain.usecase.GetHearitUseCase
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.launch

class PlayerDetailViewModel(
    private val hearitId: Long,
    private val getHearitUseCase: GetHearitUseCase,
) : ViewModel() {
    private val _hearit: MutableLiveData<Hearit> = MutableLiveData()
    val hearit: LiveData<Hearit> = _hearit

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            getHearitUseCase(hearitId)
                .onSuccess { _hearit.value = it }
                .onFailure { _toastMessage.value = R.string.player_detail_toast_hearit_load_fail }
        }
    }
}
