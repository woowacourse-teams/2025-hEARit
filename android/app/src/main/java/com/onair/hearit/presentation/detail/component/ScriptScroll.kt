package com.onair.hearit.presentation.detail.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.onair.hearit.domain.model.ScriptLine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlin.math.abs

@Stable
class ScrollState(
    val listState: LazyListState,
    private val scriptLinesProvider: () -> List<ScriptLine>,
) {
    var isAuto: Boolean by mutableStateOf(false)
        private set

    var lastCentered: Int by mutableIntStateOf(-1)
        private set

    private suspend fun awaitLayoutReady() {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.isNotEmpty() }
            .filter { isNotEmpty: Boolean -> isNotEmpty }
            .first()
    }

    private fun viewportCenter(layoutInfo: LazyListLayoutInfo): Int {
        val start: Int = layoutInfo.viewportStartOffset
        val end: Int = layoutInfo.viewportEndOffset
        return (start + end) / 2
    }

    private fun itemCenter(
        layoutInfo: LazyListLayoutInfo,
        index: Int,
    ): Int? {
        val info =
            layoutInfo.visibleItemsInfo.firstOrNull { itemInfo -> itemInfo.index == index }
                ?: return null
        return info.offset + info.size / 2
    }

    suspend fun centerVariableHeight(targetIndex: Int) {
        val scriptLines: List<ScriptLine> = scriptLinesProvider()
        if (targetIndex !in scriptLines.indices) return

        awaitLayoutReady()

        isAuto = true
        try {
            var layoutInfo: LazyListLayoutInfo = listState.layoutInfo

            if (layoutInfo.visibleItemsInfo.none { itemInfo -> itemInfo.index == targetIndex }) {
                listState.scrollToItem(targetIndex, 0)
                awaitLayoutReady()
                layoutInfo = listState.layoutInfo
            }

            val viewportCenter: Int = viewportCenter(layoutInfo)
            val itemCenter: Int = itemCenter(layoutInfo, targetIndex) ?: return
            val distancePx: Int = itemCenter - viewportCenter

            if (abs(distancePx) > SCROLL_DISTANCE_THRESHOLD_PX) {
                val animationSpec: AnimationSpec<Float> =
                    tween(
                        durationMillis = AUTO_CENTER_ANIMATION_DURATION_MILLIS,
                    )
                listState.animateScrollBy(
                    value = distancePx.toFloat(),
                    animationSpec = animationSpec,
                )
            }

            lastCentered = targetIndex
        } finally {
            isAuto = false
        }
    }

    companion object {
        private const val SCROLL_DISTANCE_THRESHOLD_PX: Int = 2
        private const val AUTO_CENTER_ANIMATION_DURATION_MILLIS: Int = 650
    }
}

@Composable
fun rememberScriptScrollState(scriptLinesProvider: () -> List<ScriptLine>): ScrollState {
    val listState: LazyListState = rememberLazyListState()
    return remember { ScrollState(listState, scriptLinesProvider) }
}

@Composable
fun rememberDetailScriptScrollState(scriptLines: List<ScriptLine>): ScrollState {
    val latestLines: List<ScriptLine> by rememberUpdatedState(scriptLines)
    return rememberScriptScrollState(scriptLinesProvider = { latestLines })
}

@Composable
fun AutoCenterOnHighlight(
    scrollState: ScrollState,
    highlightedIndex: Int,
    totalCount: Int,
) {
    LaunchedEffect(highlightedIndex, totalCount) {
        if (highlightedIndex !in 0 until totalCount) return@LaunchedEffect
        if (scrollState.isAuto) return@LaunchedEffect
        if (scrollState.lastCentered == highlightedIndex) return@LaunchedEffect

        scrollState.centerVariableHeight(highlightedIndex)
    }
}
