package com.onair.hearit.presentation.detail.script

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onair.hearit.domain.model.ScriptLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ScriptViewModel : ViewModel() {
    private val _highlightedId: MutableStateFlow<Long?> = MutableStateFlow(null)
    val highlightedId: StateFlow<Long?> = _highlightedId

    private val _isUserScrolling: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isUserScrolling: StateFlow<Boolean> = _isUserScrolling

    private val _followModeEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val followModeEnabled: StateFlow<Boolean> = _followModeEnabled

    private val lastUserScrollEpochMillis: MutableStateFlow<Long> = MutableStateFlow(0L)
    private val latestScriptsSnapshot: MutableStateFlow<List<ScriptLine>> =
        MutableStateFlow(emptyList())

    val highlightedIndex: StateFlow<Int> =
        combine(
            _highlightedId,
            latestScriptsSnapshot,
        ) { highlightedIdValue: Long?, scripts: List<ScriptLine> ->
            highlightedIdValue?.let { id -> scripts.indexOfFirst { it.id == id } } ?: -1
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STARTED_TIMEOUT_MS), -1)

    fun onUserScrollStateChange(isScrolling: Boolean) {
        _isUserScrolling.value = isScrolling
        if (isScrolling) {
            lastUserScrollEpochMillis.value = currentTimeMillis()
        }
    }

    fun stopFollowMode() {
        _followModeEnabled.value = false
    }

    fun resumeFollowMode() {
        _followModeEnabled.value = true
    }

    fun tick(
        currentPositionMillis: Long,
        currentScripts: List<ScriptLine>,
    ) {
        latestScriptsSnapshot.value = currentScripts

        if (_isUserScrolling.value) {
            val now: Long = currentTimeMillis()
            val idleReached: Boolean =
                now - lastUserScrollEpochMillis.value > USER_SCROLL_IDLE_THRESHOLD_MS
            if (idleReached && currentScripts.isNotEmpty()) {
                _isUserScrolling.value = false
            }
        }

        val currentItem: ScriptLine? =
            currentScripts.firstOrNull { script ->
                currentPositionMillis in script.start until script.end
            }
        _highlightedId.value = currentItem?.id
    }

    private fun currentTimeMillis(): Long = System.currentTimeMillis()

    companion object {
        const val USER_SCROLL_IDLE_THRESHOLD_MS: Long = 3_000L
        private const val STARTED_TIMEOUT_MS: Long = 5_000L
    }
}
