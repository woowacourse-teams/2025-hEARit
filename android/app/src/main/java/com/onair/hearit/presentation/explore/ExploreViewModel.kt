package com.onair.hearit.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.repository.ExploreDataStoreRepository
import com.onair.hearit.domain.usecase.GetExploreHearitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val shouldPlayAnimation: Boolean = false,
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
    private val exploreDataStoreRepository: ExploreDataStoreRepository,
    private val getExploreHearit: GetExploreHearitUseCase,
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
            val savedCursorId = exploreDataStoreRepository.getLastCursorId().getOrDefault(null)
            lastPosition = exploreDataStoreRepository.getLastPosition().getOrDefault(0L)

            val fetchCursorId =
                if (savedCursorId != null && savedCursorId > 0) {
                    savedCursorId - 1
                } else {
                    0L
                }

            fetchData(fetchCursorId, isFirstFetch = true)
        }
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
                getExploreHearit(cursorId)
                    .onSuccess { result ->
                        // 저장된 커서로 불렀는데 빈 값인 경우 (삭제 등) 처음부터 로드
                        if (result.isEmpty() && isFirstFetch && cursorId != 0L) {
                            isLoadingPage = false
                            fetchData(0L, isFirstFetch = true)
                            return@launch
                        }

                        _uiState.update { state ->
                            val updatedList =
                                if (isFirstFetch) {
                                    result
                                } else {
                                    (state.shortsHearits + result).distinctBy { it.id }
                                }
                            state.copy(shortsHearits = updatedList)
                        }

                        nextCursorId = result.lastOrNull()?.cursorId
                        isEndOfFeed = result.isEmpty()

                        // 첫 로딩 성공 시 즉시 재생 실행
                        if (isFirstFetch && result.isNotEmpty()) {
                            // index가 0인 경우에도 play가 호출되도록 강제함
                            onPageChanged(0, isRestoring = true)
                        }
                    }.onFailure {
                        _sideEffect.emit(ExploreSideEffect.ShowToast(R.string.explore_toast_random_hearits_load_fail))
                    }
            } catch (e: Exception) {
                Timber.e(e, "fetchData Error")
                _sideEffect.emit(ExploreSideEffect.ShowToast(R.string.explore_toast_shorts_hearits_load_fail))
            } finally {
                isLoadingPage = false
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onPageChanged(
        pageIndex: Int,
        isRestoring: Boolean = false,
    ) {
        val items = _uiState.value.shortsHearits
        val item = items.getOrNull(pageIndex) ?: return

        // 현재 인덱스 업데이트
        _uiState.update { it.copy(currentPageIndex = pageIndex) }

        // 사용자가 직접 넘기는 경우에만 재생 위치를 0으로 초기화
        if (!isRestoring) {
            lastPosition = 0L
        }

        // 현재 위치 저장
        viewModelScope.launch {
            exploreDataStoreRepository.saveLastCursorId(item.cursorId)
            exploreDataStoreRepository.saveLastPosition(lastPosition)
        }

        item.audioUrl?.let { url ->
            Timber.d("Playing audio at position: $lastPosition")
            playerManager.play(url, lastPosition)
        }

        maybeLoadMore(currentIndex = pageIndex, totalCount = items.size)
    }

    fun onPositionChanged(position: Long) {
        playerManager.seekTo(position)
        lastPosition = position
        viewModelScope.launch {
            exploreDataStoreRepository.saveLastPosition(position)
        }
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

    fun onPause() {
        if (_uiState.value.shortsHearits.isEmpty()) return
        lastPosition = playerManager.currentPosition.value
        viewModelScope.launch {
            exploreDataStoreRepository.saveLastPosition(lastPosition)
        }
        playerManager.pause()
    }

    private fun maybeLoadMore(
        currentIndex: Int,
        totalCount: Int,
    ) {
        if (isLoadingPage || isEndOfFeed || totalCount <= 0) return
        // 3개 남았을 때 추가 로드
        if (currentIndex >= totalCount - 3) {
            fetchData(nextCursorId ?: 0L)
        }
    }

    fun loadAnimation() {
        viewModelScope.launch {
            exploreDataStoreRepository
                .shouldShowAnimation()
                .onSuccess { shouldShow ->
                    _uiState.update { it.copy(shouldPlayAnimation = shouldShow) }
                }.onFailure {
                    _uiState.update { it.copy(shouldPlayAnimation = false) }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.stop()
    }
}
