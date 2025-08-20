package com.onair.hearit.presentation.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.R
import com.onair.hearit.domain.DomainException.UserNotRegistered
import com.onair.hearit.domain.model.CursorInfo
import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.ShortsHearit
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.ExploreDataStoreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.usecase.GetShortsHearitUseCase
import com.onair.hearit.presentation.SingleLiveData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class ExploreViewModel(
    private val hearitRepository: HearitRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val exploreDataStoreRepository: ExploreDataStoreRepository,
    private val getShortsHearitUseCase: GetShortsHearitUseCase,
) : ViewModel() {
    private val _shortsHearits = MutableLiveData<List<ShortsHearit>>()
    val shortsHearits: LiveData<List<ShortsHearit>> = _shortsHearits

    private val _bookmarkId = MutableLiveData<Map<Long, Long?>>()
    val bookmarkId: LiveData<Map<Long, Long?>> = _bookmarkId

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _showLoginDialog = SingleLiveData<Unit>()
    val showLoginDialog: LiveData<Unit> = _showLoginDialog

    private val _shouldPlayAnimation = MutableLiveData<Boolean>()
    val shouldPlayAnimation: LiveData<Boolean> = _shouldPlayAnimation

    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private lateinit var cursorInfo: CursorInfo
    private var isFetchingData = false

    private val _currentIndex = MutableLiveData<Int>(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _showHearitError = MutableLiveData<Boolean>(false)
    val showHearitError: LiveData<Boolean> = _showHearitError

    private var lastPlayerPosition: Long = 0L
    private var lastItem: ShortsHearit? = null

    init {
        _isLoading.value = true
        fetchData(0)
    }

    fun fetchNextPage() {
        if (isFetchingData || cursorInfo.isEmpty) return
        fetchData(cursorInfo.cursorId)
    }

    fun onPause(
        position: Int,
        lastPlayerPosition: Long,
        itemCount: Int,
    ) {
        if (position == itemCount - 1) {
            reFetchData()
        } else {
            if (position != RecyclerView.NO_POSITION) {
                onPageSnapped(position)
            }
        }
        saveLastPlayerPosition(lastPlayerPosition)
        _showHearitError.value = false
    }

    fun onPageSnapped(index: Int) {
        if (_currentIndex.value != index) {
            _currentIndex.value = index
        }
    }

    fun saveLastPlayerPosition(position: Long) {
        lastPlayerPosition = position
    }

    fun getLastPlayerPosition(): Long {
        val position = lastPlayerPosition
        lastPlayerPosition = 0L
        return position
    }

    fun toggleBookmark(
        hearitId: Long,
        onFinished: (bookmarkId: Long?) -> Unit,
    ) {
        val currentBookmarkId = _bookmarkId.value?.get(hearitId)
        if (currentBookmarkId == null) {
            addBookmark(hearitId, onFinished)
        } else {
            deleteBookmark(hearitId, currentBookmarkId, onFinished)
        }
    }

    fun loadAnimation() {
        viewModelScope.launch {
            exploreDataStoreRepository
                .shouldShowAnimation()
                .onSuccess { shouldShow ->
                    _shouldPlayAnimation.value = shouldShow
                }.onFailure {
                    _shouldPlayAnimation.value = false
                }
        }
    }

    fun showHearitError() {
        _shortsHearits.value = emptyList()
        _showHearitError.value = true
    }

    fun reFetchData() {
        _currentIndex.value = 0

        _shortsHearits.value?.lastOrNull()?.let { shortsLastItem ->
            val lastBookmarkId = _bookmarkId.value?.get(shortsLastItem.id)
            lastItem =
                shortsLastItem.copy(
                    bookmarkId = lastBookmarkId,
                    isBookmarked = lastBookmarkId != null,
                )
        }

        _shortsHearits.value = emptyList()
        _bookmarkId.value = emptyMap()
        fetchData(0)
        _showHearitError.value = false
    }

    private fun fetchData(cursorId: Long) {
        if (isFetchingData) return
        isFetchingData = true

        viewModelScope.launch {
            try {
                val result =
                    hearitRepository.getRandomHearits(cursorId)
                result
                    .onSuccess { randomItems ->
                        cursorInfo = randomItems.cursorInfo
                        val shortsList = buildShortsHearit(randomItems)
                        updateShortsHearit(shortsList)
                        _isLoading.value = false
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _toastMessage.value = R.string.explore_toast_random_hearits_load_fail
                    }
            } catch (e: Exception) {
                Timber.w(e)
                _toastMessage.value = R.string.explore_toast_shorts_hearits_load_fail
            } finally {
                isFetchingData = false
            }
        }
    }

    private fun addBookmark(
        hearitId: Long,
        onFinished: (bookmarkId: Long?) -> Unit,
    ) {
        viewModelScope.launch {
            bookmarkRepository
                .addBookmark(hearitId)
                .onSuccess { newBookmarkId ->
                    updateBookmarkState(hearitId, newBookmarkId)
                    onFinished(newBookmarkId)
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegistered -> {
                            _showLoginDialog.call()
                            onFinished(-1L)
                        }

                        else -> {
                            Timber.w(throwable)
                            _toastMessage.value = R.string.all_toast_add_bookmark_fail
                            onFinished(null)
                        }
                    }
                }
        }
    }

    private fun deleteBookmark(
        hearitId: Long,
        bookmarkId: Long,
        onFinished: (bookmarkId: Long?) -> Unit,
    ) {
        viewModelScope.launch {
            bookmarkRepository
                .deleteBookmark(bookmarkId)
                .onSuccess {
                    updateBookmarkState(hearitId, null)
                    onFinished(bookmarkId)
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _toastMessage.value = R.string.all_toast_delete_bookmark_fail
                    onFinished(null)
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

    private suspend fun buildShortsHearit(cursorItems: CursorResult<RandomHearit>): List<ShortsHearit> =
        coroutineScope {
            cursorItems.items
                .map { item ->
                    async { getShortsHearitUseCase(item).getOrNull() }
                }.awaitAll()
                .mapNotNull { it }
        }

    private fun updateShortsHearit(newItems: List<ShortsHearit>) {
        val combinedList =
            if (lastItem != null) {
                val uniqueNewItems = newItems.filter { it.id != lastItem?.id }
                listOf(lastItem!!) + uniqueNewItems
            } else {
                _shortsHearits.value.orEmpty() + newItems
            }

        _shortsHearits.value = combinedList
        _bookmarkId.value = combinedList.associate { it.id to it.bookmarkId }

        lastItem = null
    }
}
