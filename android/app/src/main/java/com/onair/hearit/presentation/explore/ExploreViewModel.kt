package com.onair.hearit.presentation.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.di.RepositoryProvider.dataStoreRepository
import com.onair.hearit.domain.UserNotRegisteredException
import com.onair.hearit.domain.model.CursorInfo
import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.ShortsHearit
import com.onair.hearit.domain.repository.BookmarkRepository
import com.onair.hearit.domain.repository.ExploreDataStoreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.usecase.GetShortsHearitUseCase
import com.onair.hearit.presentation.SingleLiveData
import com.onair.hearit.presentation.toBearerToken
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

    init {
        _isLoading.value = true
        setAnimation()
        fetchData(cursorId = 0)
    }

    fun fetchNextPage() {
        if (cursorInfo.isEmpty || isFetchingData) return
        fetchData(cursorInfo.cursorId)
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

    private fun setAnimation() {
        viewModelScope.launch {
            exploreDataStoreRepository
                .getExploreCount()
                .onSuccess { currentCount ->
                    if (currentCount < MAX_ANIMATION_COUNT) {
                        val newCount = currentCount + 1
                        exploreDataStoreRepository.updateExploreCount(newCount)
                        _shouldPlayAnimation.value = true
                    } else {
                        _shouldPlayAnimation.value = false
                    }
                }.onFailure { throwable ->
                    Timber.w(throwable)
                    _shouldPlayAnimation.value = false
                }
        }
    }

    private fun fetchData(cursorId: Long) {
        if (isFetchingData) return
        isFetchingData = true

        viewModelScope.launch {
            val token = dataStoreRepository.getAccessToken().getOrNull()

            try {
                val result =
                    hearitRepository.getRandomHearits(token?.toBearerToken(), cursorId)
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
            val token = dataStoreRepository.getAccessToken().getOrNull()

            bookmarkRepository
                .addBookmark(token?.toBearerToken(), hearitId)
                .onSuccess { newBookmarkId ->
                    updateBookmarkState(hearitId, newBookmarkId)
                    onFinished(newBookmarkId)
                }.onFailure { throwable ->
                    when (throwable) {
                        is UserNotRegisteredException -> {
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
            val token = dataStoreRepository.getAccessToken().getOrNull()

            bookmarkRepository
                .deleteBookmark(token?.toBearerToken(), bookmarkId)
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
        _shortsHearits.value = _shortsHearits.value.orEmpty() + newItems

        _bookmarkId.value =
            _bookmarkId.value.orEmpty().toMutableMap().apply {
                newItems.forEach { item ->
                    this[item.id] = item.bookmarkId
                }
            }
    }

    companion object {
        private const val MAX_ANIMATION_COUNT = 2
    }
}
