package com.onair.hearit.presentation.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.ShortsHearit
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.usecase.GetShortsHearitUseCase
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val hearitRepository: HearitRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val getShortsHearitUseCase: GetShortsHearitUseCase,
) : ViewModel() {
    private val _shortsHearits = MutableLiveData<List<ShortsHearit>>()
    val shortsHearits: LiveData<List<ShortsHearit>> = _shortsHearits

    private val _isBookmarked: MutableLiveData<Boolean> = MutableLiveData()
    val isBookmarked: LiveData<Boolean> = _isBookmarked

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private lateinit var paging: Paging
    private var currentPage = 0
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
                val result = hearitRepository.getRandomHearits(page)

                result
                    .onSuccess { randomItems ->
                        paging = randomItems.paging
                        val shortsList = buildShortsHearit(randomItems)
                        updateShortsHearit(shortsList, isInitial)
                        isLastPage = paging.isLast
                    }.onFailure {
                        _toastMessage.value = R.string.explore_toast_random_hearits_load_fail
                    }
            } catch (_: Exception) {
                _toastMessage.value = R.string.explore_toast_shorts_hearits_load_fail
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun buildShortsHearit(pageItems: PageResult<RandomHearit>): List<ShortsHearit> =
        coroutineScope {
            pageItems.items
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

    private fun addBookmark(hearitId: Long) {
        viewModelScope.launch {
            bookmarkRepository
                .addBookmark(hearitId)
                .onSuccess { _isBookmarked.value = true }
                .onFailure {
                    _isBookmarked.value = false
                    _toastMessage.value = R.string.all_toast_add_bookmark_fail
                }
        }
    }
}
