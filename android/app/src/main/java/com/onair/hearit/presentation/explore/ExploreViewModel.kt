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

    private val _bookmarkId = MutableLiveData<Map<Long, Long?>>()
    val bookmarkId: LiveData<Map<Long, Long?>> = _bookmarkId

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

    fun toggleBookmark(hearitId: Long) {
        val currentBookmarkIdMap = _bookmarkId.value.orEmpty().toMutableMap()

        val bookmarkId = currentBookmarkIdMap[hearitId]

        if (bookmarkId != null) {
            deleteBookmark(hearitId, bookmarkId)
        } else {
            addBookmark(hearitId)
        }
    }

    private fun deleteBookmark(
        hearitId: Long,
        bookmarkId: Long,
    ) {
        viewModelScope.launch {
            bookmarkRepository
                .deleteBookmark(bookmarkId)
                .onSuccess {
                    updateBookmarkState(hearitId, null)
                }.onFailure {
                    _toastMessage.value = R.string.all_toast_delete_bookmark_fail
                }
        }
    }

    private fun addBookmark(hearitId: Long) {
        viewModelScope.launch {
            bookmarkRepository
                .addBookmark(hearitId)
                .onSuccess { newBookmarkId ->
                    updateBookmarkState(hearitId, newBookmarkId)
                }.onFailure {
                    _toastMessage.value = R.string.all_toast_add_bookmark_fail
                }
        }
    }

    private fun updateBookmarkState(
        hearitId: Long,
        bookmarkId: Long?,
    ) {
        val currentBookmarkId = _bookmarkId.value.orEmpty().toMutableMap()
        currentBookmarkId[hearitId] = bookmarkId
        _bookmarkId.value = currentBookmarkId
    }

    private fun fetchData(
        page: Int,
        isInitial: Boolean,
    ) {
        isLoading = true
        viewModelScope.launch {
            try {
                val result = hearitRepository.getRandomHearits(0)
                result
                    .onSuccess { randomItems ->
                        paging = randomItems.paging
                        val shortsList = buildShortsHearit(randomItems)
                        updateShortsHearit(shortsList, isInitial)

                        // currentPage++
                        // isLastPage = paging.isLast
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

        _bookmarkId.value =
            if (isInitial) {
                newItems.associate { it.id to it.bookmarkId }
            } else {
                _bookmarkId.value.orEmpty().toMutableMap().apply {
                    newItems.forEach { item ->
                        this[item.id] = item.bookmarkId
                    }
                }
            }
    }
}
