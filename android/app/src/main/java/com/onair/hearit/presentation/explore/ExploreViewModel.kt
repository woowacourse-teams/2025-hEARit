package com.onair.hearit.presentation.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.RandomHearit
import com.onair.hearit.domain.ShortsHearit
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.usecase.GetShortsHearitUseCase
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val hearitRepository: HearitRepository,
    private val getShortsHearitUseCase: GetShortsHearitUseCase,
) : ViewModel() {
    private val _shortsHearits = MutableLiveData<List<ShortsHearit>>()
    val shortsHearits: LiveData<List<ShortsHearit>> = _shortsHearits

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private var currentPage = 0

    // 현재 테스트용 페이지 사이즈
    private val pageSize = 5
    private var isLastPage = false
    private var isLoading = false

    init {
        fetchData(page = 0, isInitial = true)
    }

    fun fetchNextPage() {
        if (isLoading || isLastPage) return
        fetchData(page = currentPage, isInitial = false)
    }

    private fun fetchData(
        page: Int,
        isInitial: Boolean,
    ) {
        isLoading = true
        viewModelScope.launch {
            try {
                val result = hearitRepository.getRandomHearits(page, pageSize)

                result
                    .onSuccess { randomItems ->
                        val shortsList = buildShortsHearit(randomItems)

                        updateShortsHearit(shortsList, isInitial)

                        // API 스펙 변경되면 수정할 예정 -> 현재는 디폴트 값 0으로 보내는 중
                        // currentPage++

                        // 서버에서 API 스펙 변경되면 수정할 예정 -> 현재는 그냥 5개 불렀을 때 5개 보다 적게 내려오면 마지막이라고 판단함
                        isLastPage = shortsList.size < pageSize
                    }.onFailure {
                        _toastMessage.value = R.string.explore_toast_random_hearits_load_fail
                    }
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun buildShortsHearit(items: List<RandomHearit>): List<ShortsHearit> =
        coroutineScope {
            items
                .map { item ->
                    async { getShortsHearitUseCase(item).getOrNull() }
                }.awaitAll()
                .mapNotNull { it }
        }

    private fun updateShortsHearit(
        newItems: List<ShortsHearit>,
        isInitial: Boolean,
    ) {
        _shortsHearits.value =
            if (isInitial) {
                newItems
            } else {
                _shortsHearits.value.orEmpty() + newItems
            }
    }
}
