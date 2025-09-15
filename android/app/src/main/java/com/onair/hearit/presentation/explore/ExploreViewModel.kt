package com.onair.hearit.presentation.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.RandomHearit
import com.onair.hearit.domain.model.ShortsHearit
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
    private val exploreDataStoreRepository: ExploreDataStoreRepository,
    private val getShortsHearitUseCase: GetShortsHearitUseCase,
) : ViewModel() {
    private val _shortsHearits = MutableLiveData<List<ShortsHearit>>()
    val shortsHearits: LiveData<List<ShortsHearit>> = _shortsHearits

    private val _toastMessage = SingleLiveData<Int>()
    val toastMessage: LiveData<Int> = _toastMessage

    private val _showLoginDialog = SingleLiveData<Unit>()
    val showLoginDialog: LiveData<Unit> = _showLoginDialog

    private val _shouldPlayAnimation = MutableLiveData<Boolean>()
    val shouldPlayAnimation: LiveData<Boolean> = _shouldPlayAnimation

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private var isLoadingPage: Boolean = false // 서버 페이지 로딩 중 여부
    private var isEndOfFeed: Boolean = false // 더 이상 페이지가 없는지
    private var nextCursorId: Long? = -1L // 다음 페이지 시작 커서

    private var resumeItem: ShortsHearit? = null // 복귀 시 표시할 아이템
    private var resumePositionMs: Long = 0L // 복귀 시 플레이어 시작 위치(ms)
    private var resumeScheduled: Boolean = false // 다음 attach 때 재개 예정인지

    init {
        _isLoading.value = true
        fetchData(0L)
    }

    // 다음 페이지 요청
    fun loadNextPage() {
        if (isLoadingPage || isEndOfFeed) return
        fetchData(nextCursorId ?: 0L)
    }

    // 탭 이탈 시점: 복귀를 위한 아이템/플레이어 위치를 저장하고 재개 예약
    fun scheduleResume(
        resumeIndex: Int,
        playerPositionMs: Long,
    ) {
        _shortsHearits.value?.getOrNull(resumeIndex)?.let { item ->
            resumeItem = item
        }
        resumePositionMs = playerPositionMs
        resumeScheduled = true
    }

    // attach 시점: 예약돼 있으면 리스트를 재로딩하고 즉시 프리패치 트리거
    fun resumeIfScheduled() {
        if (!resumeScheduled) return
        resumeScheduled = false

        _shortsHearits.value = emptyList()
        _isLoading.value = true
        isLoadingPage = false

        val startCursor = resumeItem?.cursorId ?: 0L
        fetchData(startCursor)

        // 마지막 하나만 먼저 들어오는 순간 비어 보이는 느낌을 줄이기 위해 즉시 프리패치
        maybeLoadMore(currentIndex = 0, totalCount = 1)
    }

    // 1회성 복원 위치(ms) 소비 후 0으로 리셋
    fun consumeResumePositionMs(): Long {
        val pos = resumePositionMs
        resumePositionMs = 0L
        return pos
    }

    // 프리패치 정책: 끝에서 N개(=3) 이내면 다음 페이지, 끝났으면 0부터 다시 로드
    fun maybeLoadMore(
        currentIndex: Int,
        totalCount: Int,
    ) {
        if (isLoadingPage || totalCount <= 0) return

        // currentIndex가 totalCount-3 이상이면 nearEnd
        val nearEnd = currentIndex >= maxOf(0, totalCount - 3)
        if (!nearEnd) return

        // 피드의 마지막이면 -> 서버에서 넘어온 데이터가 isEmpty이면.
        if (isEndOfFeed) {
            isEndOfFeed = false
            nextCursorId = -1L
            fetchData(0L)
        } else {
            loadNextPage()
        }
    }

    // 스와이프 가이드 애니메이션 노출 여부 로드
    fun loadAnimation() {
        viewModelScope.launch {
            exploreDataStoreRepository
                .shouldShowAnimation()
                .onSuccess { _shouldPlayAnimation.value = it }
                .onFailure { _shouldPlayAnimation.value = false }
        }
    }

    private fun fetchData(cursorId: Long) {
        if (isLoadingPage) return
        isLoadingPage = true

        viewModelScope.launch {
            try {
                val result = hearitRepository.getRandomHearits(cursorId)
                result
                    .onSuccess { randomItems ->
                        isEndOfFeed = randomItems.isEmpty
                        nextCursorId = randomItems.items.lastOrNull()?.cursorId

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
                isLoadingPage = false
            }
        }
    }

    private suspend fun buildShortsHearit(cursorItems: CursorResult<RandomHearit>): List<ShortsHearit> =
        coroutineScope {
            cursorItems.items
                .map { item -> async { getShortsHearitUseCase(item).getOrNull() } }
                .awaitAll()
                .mapNotNull { it }
        }

    private fun updateShortsHearit(newItems: List<ShortsHearit>) {
        val combined =
            if (resumeItem != null) {
                val uniqueNew = newItems.filter { it.id != resumeItem?.id }
                (listOf(resumeItem!!) + uniqueNew)
            } else {
                _shortsHearits.value.orEmpty() + newItems
            }
        _shortsHearits.value = combined.distinctBy { it.id }
        resumeItem = null
    }
}
