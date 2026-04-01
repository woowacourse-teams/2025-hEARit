package com.onair.hearit.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.CursorResult
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.repository.ExploreRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.usecase.GetExploreHearitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ExploreUiState(
    val shortsHearits: List<ExploreHearit> = emptyList(),
    val isLoading: Boolean = true,
    val currentPageIndex: Int = 0,
    val showGuideAnimation: Boolean = false,
    val showLoginDialog: Boolean = false,
)

sealed interface ExploreSideEffect {
    data class ShowToast(
        val messageResId: Int,
    ) : ExploreSideEffect

    data object NavigateToBack : ExploreSideEffect

    data class NavigateToDetail(
        val hearitId: Long,
        val lastPosition: Long,
    ) : ExploreSideEffect
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val playerManager: ExplorePlayerManager,
    private val exploreRepository: ExploreRepository,
    private val getExploreHearit: GetExploreHearitUseCase,
    private val hearitRepository: HearitRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<ExploreSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    val currentPosition = playerManager.currentPosition
    val isPlaying = playerManager.isPlaying
    val speed = playerManager.speed
    val duration = playerManager.duration
    val isPlaybackEnded = playerManager.isPlaybackEnded

    private var isLoadingPage: Boolean = false
    private var isEndOfFeed: Boolean = false
    private var nextCursorId: Long? = -1L

    private var lastPosition: Long = 0L

    // 복귀(Resume) 관련 변수
    private var resumeItem: ExploreHearit? = null
    private var resumePositionMs: Long = 0L
    private var resumeScheduled: Boolean = false

    init {
        loadInitialState()
        observePlaybackEnded()
    }

    private fun observePlaybackEnded() {
        viewModelScope.launch {
            isPlaybackEnded.collect { isEnded ->
                if (isEnded) {
                    val currentIndex = _uiState.value.currentPageIndex
                    if (currentIndex < _uiState.value.shortsHearits.size - 1) {
                        onPageChanged(currentIndex + 1)
                    }
                }
            }
        }
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            fetchData(0L, isFirstFetch = true)
        }
    }

    fun scheduleResume(resumeIndex: Int) {
        _uiState.value.shortsHearits.getOrNull(resumeIndex)?.let { item ->
            resumeItem = item
        }
        resumePositionMs = lastPosition

        resumeScheduled = true
        playerManager.pause()
    }

    // 다시 돌아왔을 때 호출
    fun resumeIfScheduled() {
        if (!resumeScheduled) return
        resumeScheduled = false

        _uiState.update { it.copy(shortsHearits = emptyList(), isLoading = true) }
        isLoadingPage = false

        val startCursor = resumeItem?.cursorId ?: 0L
        fetchData(startCursor, isFirstFetch = true)
    }

    private fun fetchData(
        cursorId: Long,
        isFirstFetch: Boolean = false,
    ) {
        if (isLoadingPage) return
        isLoadingPage = true
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                hearitRepository
                    .getExploreHearits(cursorId)
                    .onSuccess { cursorResult ->
                        if (cursorResult.items.isEmpty() && isFirstFetch && cursorId != 0L) {
                            isLoadingPage = false
                            fetchData(0L, isFirstFetch = true)
                            return@launch
                        }

                        val newItems = buildShortsItems(cursorResult)

                        _uiState.update { state ->
                            val updatedList =
                                if (resumeItem != null) {
                                    (listOf(resumeItem!!) + newItems.filter { it.id != resumeItem?.id })
                                } else if (isFirstFetch) {
                                    newItems
                                } else {
                                    (state.shortsHearits + newItems)
                                }.distinctBy { it.id }

                            state.copy(shortsHearits = updatedList)
                        }

                        isEndOfFeed = cursorResult.items.isEmpty()
                        nextCursorId = cursorResult.items.lastOrNull()?.cursorId
                        resumeItem = null // 처리 완료 후 초기화

                        if (isFirstFetch && _uiState.value.shortsHearits.isNotEmpty()) {
                            onPageChanged(0, isRestoring = true)
                        }
                    }.onFailure {
                        _sideEffect.emit(ExploreSideEffect.ShowToast(R.string.explore_toast_random_hearits_load_fail))
                    }
            } catch (e: Exception) {
                Timber.e(e)
                _sideEffect.emit(ExploreSideEffect.ShowToast(R.string.explore_toast_shorts_hearits_load_fail))
            } finally {
                isLoadingPage = false
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun buildShortsItems(cursorResult: CursorResult<ExploreHearit>): List<ExploreHearit> =
        coroutineScope {
            cursorResult.items
                .map { item -> async { getExploreHearit(item).getOrNull() } }
                .awaitAll()
                .mapNotNull { it }
        }

    fun onPageChanged(
        pageIndex: Int,
        isRestoring: Boolean = false,
    ) {
        val items = _uiState.value.shortsHearits
        val item = items.getOrNull(pageIndex) ?: return

        _uiState.update { it.copy(currentPageIndex = pageIndex) }

        // 복원 중일 때는 저장된 resumePositionMs 사용 후 0으로 리셋
        val startPos =
            if (isRestoring) {
                val pos = if (resumePositionMs > 0) resumePositionMs else lastPosition
                resumePositionMs = 0L
                pos
            } else {
                0L
            }
        lastPosition = startPos

        item.audioUrl?.let { url ->
            playerManager.play(url, startPos)
        }

        maybeLoadMore(currentIndex = pageIndex, totalCount = items.size)
    }

    fun onPositionChanged(position: Long) {
        playerManager.seekTo(position)
        lastPosition = position
    }

    fun onPlayerStateChanged() {
        if (playerManager.isPlaying.value) playerManager.pause() else playerManager.resume()
    }

    fun onSetPlayerSpeed() {
        if (!playerManager.isPlaying.value) return
        val newSpeed = if (playerManager.speed.value == 1.0f) 2.0f else 1.0f
        playerManager.setPlaybackSpeed(newSpeed)
    }

    fun onHearitSelected(id: Long) {
        val currentPos = playerManager.currentPosition.value
        viewModelScope.launch {
            _sideEffect.emit(ExploreSideEffect.NavigateToDetail(id, currentPos))
        }
    }

    private fun maybeLoadMore(
        currentIndex: Int,
        totalCount: Int,
    ) {
        if (isLoadingPage || totalCount <= 0) return

        val nearEnd = currentIndex >= maxOf(0, totalCount - 3)
        if (!nearEnd) return

        if (isEndOfFeed) {
            isEndOfFeed = false
            nextCursorId = -1L
            fetchData(0L)
        } else {
            fetchData(nextCursorId ?: 0L)
        }
    }

    fun loadAnimation() {
        viewModelScope.launch {
            exploreRepository
                .shouldShowAnimation()
                .onSuccess { shouldShow ->
                    _uiState.update { it.copy(showGuideAnimation = shouldShow) }
                }.onFailure {
                    _uiState.update { it.copy(showGuideAnimation = false) }
                }
        }
    }

    fun dismissAnimation() {
        _uiState.update { it.copy(showGuideAnimation = false) }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.stop()
    }
}
