package com.onair.hearit.presentation.detail.script

import androidx.lifecycle.ViewModel
import com.onair.hearit.domain.model.ScriptLine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class ScriptViewModel @Inject constructor(
    private val clock: Clock,
) : ViewModel() {
    private val _highlightedId: MutableStateFlow<Long?> = MutableStateFlow(null)
    val highlightedId: StateFlow<Long?> = _highlightedId

    private val _isUserScrolling: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isUserScrolling: StateFlow<Boolean> = _isUserScrolling

    private val _followModeEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val followModeEnabled: StateFlow<Boolean> = _followModeEnabled

    private val lastUserScrollEpochMillis: MutableStateFlow<Long> = MutableStateFlow(0L)

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
        if (_isUserScrolling.value) {
            val now: Long = currentTimeMillis()
            val idleReached: Boolean =
                now - lastUserScrollEpochMillis.value > USER_SCROLL_IDLE_THRESHOLD_MS
            if (idleReached && currentScripts.isNotEmpty()) {
                _isUserScrolling.value = false
            }
        }

        val currentIndex: Int =
            currentScripts.indexOfFirst { script ->
                currentPositionMillis in script.start until script.end
            }
        val newHighlightedId: Long? =
            if (currentIndex >= 0) currentScripts[currentIndex].id else null
        if (_highlightedId.value != newHighlightedId) _highlightedId.value = newHighlightedId
    }

    private fun currentTimeMillis(): Long = clock.millis()

    companion object {
        const val USER_SCROLL_IDLE_THRESHOLD_MS: Long = 3_000L
        private const val STARTED_TIMEOUT_MS: Long = 5_000L
    }
}
