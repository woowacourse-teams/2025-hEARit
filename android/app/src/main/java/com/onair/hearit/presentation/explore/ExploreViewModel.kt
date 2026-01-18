package com.onair.hearit.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.R
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.repository.ExploreDataStoreRepository
import com.onair.hearit.domain.repository.HearitRepository
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
    private val hearitRepository: HearitRepository,
    private val exploreDataStoreRepository: ExploreDataStoreRepository,
    private val getExploreHearitUseCase: GetExploreHearitUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    // 2. 일회성 이벤트 (Toast 등 - SharedFlow 활용)
    private val _sideEffect = MutableSharedFlow<ExploreSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    val currentPosition = playerManager.currentPosition
    val isPlaying = playerManager.isPlaying
    val speed = playerManager.speed
    val duration = playerManager.duration
    val isPlaybackEnded = playerManager.isPlaybackEnded

    private var isLoadingPage: Boolean = false // 서버 페이지 로딩 중 여부
    private var isEndOfFeed: Boolean = false // 더 이상 페이지가 없는지
    private var nextCursorId: Long? = -1L // 다음 페이지 시작 커서

    private var resumeItem: ExploreHearit? = null // 복귀 시 표시할 아이템
    private var resumePositionMs: Long = 0L // 복귀 시 플레이어 시작 위치(ms)
    private var resumeScheduled: Boolean = false // 다음 attach 때 재개 예정인지

    init {
        fetchData(0L)
        observePlaybackEnded()
    }

    private fun observePlaybackEnded() {
        viewModelScope.launch {
            isPlaybackEnded.collect { isEnded ->
                if (isEnded) {
                    val totalCount = _uiState.value.shortsHearits.size
                    val currentIndex = _uiState.value.currentPageIndex

                    if (currentIndex < totalCount - 1) {
                        _uiState.update { it.copy(currentPageIndex = currentIndex + 1) }
                    }
                }
            }
        }
    }

    /**
     * 아이템 클릭 시 상세 화면으로 이동하는 이벤트 발생
     */
    fun onHearitSelected(id: Long) {
        // 현재 PlayerManager가 들고 있는 실시간 재생 위치 가져오기
        val currentPosition = playerManager.currentPosition.value

        viewModelScope.launch {
            _sideEffect.emit(
                ExploreSideEffect.NavigateToDetail(
                    hearitId = id,
                    lastPosition = currentPosition,
                ),
            )
        }
    }

    fun onPlayerStateChanged() {
        if (playerManager.isPlaying.value) {
            playerManager.pause()
        } else {
            playerManager.resume()
        }
    }

    fun onSetPlayerSpeed() {
        if (!playerManager.isPlaying.value) return

        // 재생 중일 때만 속도 전환 로직 수행
        if (playerManager.speed.value == 1.0f) {
            playerManager.setPlaybackSpeed(2.0f)
        } else {
            playerManager.setPlaybackSpeed(1.0f)
        }
    }

    fun onPositionChanged(position: Long) {
        playerManager.seekTo(position)
    }

    // 다음 페이지 요청
    fun loadNextPage() {
        if (isLoadingPage || isEndOfFeed) return
        fetchData(nextCursorId ?: 0L)
    }

    /**
     * [ON_RESUME] 화면으로 돌아왔을 때 호출: 저장된 상태가 있다면 재생 재개.
     */
    fun resumeIfScheduled() {
        // 예약된 상태가 아니면 아무것도 하지 않음
        if (!resumeScheduled) return

        // onPageChanged 로직을 재사용하여 저장된 위치에서 재생 시작
        // consumeResumePositionMs() 내부에서 resumePositionMs가 0으로 초기화됨
        onPageChanged(_uiState.value.currentPageIndex)
        Timber.d("resumeIfScheduled called. Resuming at index ${_uiState.value.currentPageIndex}")

        // 재개 로직을 수행했으므로 예약 상태 해제
        resumeScheduled = false
    }

    // 1회성 복원 위치(ms) 소비 후 0으로 리셋
    fun consumeResumePositionMs(): Long {
        val position = resumePositionMs
        resumePositionMs = 0L
        return position
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

    fun onPause() {
        if (_uiState.value.shortsHearits.isEmpty()) return

        val currentIndex = _uiState.value.currentPageIndex
        val currentPosition = playerManager.currentPosition.value

        // scheduleResume 함수를 호출하여 상태 저장
        scheduleResume(currentIndex, currentPosition)

        // 플레이어 일시정지
        playerManager.pause()
    }

    private fun scheduleResume(
        resumeIndex: Int,
        playerPositionMs: Long,
    ) {
        _uiState.value.shortsHearits.getOrNull(resumeIndex)?.let { item ->
            resumeItem = item
        }
        resumePositionMs = playerPositionMs
        resumeScheduled = true // 예약 플래그를 true로 설정
    }

    // 스와이프 가이드 애니메이션 노출 여부 로드
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

    private fun fetchData(cursorId: Long) {
        if (isLoadingPage) return
        isLoadingPage = true

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                hearitRepository
                    .getExploreHearits(cursorId)
                    .onSuccess { randomItems ->
                        isEndOfFeed = randomItems.isEmpty
                        nextCursorId = randomItems.items.lastOrNull()?.cursorId

                        // UseCase가 내부적으로 async/awaitAll을 수행하여 리스트를 반환
                        val newShorts = getExploreHearitUseCase(randomItems.items)

                        _uiState.update { currentState ->
                            val combined =
                                if (resumeItem != null) {
                                    (listOf(resumeItem!!) + newShorts.filter { it.id != resumeItem?.id })
                                } else {
                                    currentState.shortsHearits + newShorts
                                }

                            val updatedList = combined.distinctBy { it.id }

                            // 처음 로딩 시 첫 아이템 재생
                            if (currentState.shortsHearits.isEmpty() && updatedList.isNotEmpty()) {
                                launch { onPageChanged(0) }
                            }

                            currentState.copy(
                                shortsHearits = updatedList,
                                isLoading = false,
                            )
                        }
                        resumeItem = null
                    }.onFailure { throwable ->
                        Timber.w(throwable)
                        _sideEffect.emit(ExploreSideEffect.ShowToast(R.string.explore_toast_random_hearits_load_fail))
                    }
            } catch (e: Exception) {
                Timber.w(e)
                _sideEffect.emit(ExploreSideEffect.ShowToast(R.string.explore_toast_shorts_hearits_load_fail))
            } finally {
                isLoadingPage = false
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onPageChanged(pageIndex: Int) {
        // 현재 상태의 인덱스와 입력받은 인덱스가 다를 때만 업데이트
        if (_uiState.value.currentPageIndex != pageIndex) {
            _uiState.update { it.copy(currentPageIndex = pageIndex) }
        }

        val items = _uiState.value.shortsHearits
        if (items.isEmpty() || pageIndex >= items.size) return

        val currentItem = items[pageIndex]
        if (currentItem.audioUrl == null) return

        val startPosition =
            if (resumeScheduled && currentItem.id == resumeItem?.id) {
                consumeResumePositionMs()
            } else {
                0L
            }

        playerManager.play(currentItem.audioUrl, startPosition)
        maybeLoadMore(currentIndex = pageIndex, totalCount = items.size)
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.stop()
    }
}
