package com.onair.hearit.presentation.detail.script.component

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.onair.hearit.domain.model.ScriptLine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlin.math.abs

@Composable
fun Scripts(
    scriptLines: List<ScriptLine>,
    highlightedId: Long?,
    highlightedIndex: Int,
    isUserScrolling: Boolean,
    onLineClick: (ScriptLine) -> Unit,
    onUserScrollStateChange: (Boolean) -> Unit,
) {
    val listState = rememberLazyListState()

    var isAuto by remember { mutableStateOf(false) }
    var lastCentered by remember { mutableIntStateOf(-1) }

    suspend fun awaitLayoutReady() {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.isNotEmpty() }
            .filter { it }
            .first()
    }

    fun viewportCenter(layoutInfo: LazyListLayoutInfo): Int = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

    fun itemCenter(
        layoutInfo: LazyListLayoutInfo,
        index: Int,
    ): Int? {
        val info = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return null
        return info.offset + info.size / 2
    }

    suspend fun centerVariableHeight(index: Int) {
        if (index !in scriptLines.indices) return
        awaitLayoutReady()

        isAuto = true
        try {
            var layoutInfo = listState.layoutInfo

            // 1) 화면에 없으면 일단 배치
            if (layoutInfo.visibleItemsInfo.none { it.index == index }) {
                listState.scrollToItem(index, 0)
                awaitLayoutReady()
                layoutInfo = listState.layoutInfo
            }

            // 2) 실제 중앙 차이만큼 1회 보정
            val vpCenter = viewportCenter(layoutInfo)
            val itemCenter = itemCenter(layoutInfo, index) ?: return
            val dist = itemCenter - vpCenter
            if (abs(dist) > 2) {
                listState.animateScrollBy(dist.toFloat())
            }

            lastCentered = index
        } finally {
            isAuto = false
        }
    }

    LaunchedEffect(highlightedIndex, scriptLines.size) {
        if (isUserScrolling || isAuto) return@LaunchedEffect
        if (highlightedIndex !in scriptLines.indices) return@LaunchedEffect
        if (lastCentered == highlightedIndex) return@LaunchedEffect
        centerVariableHeight(highlightedIndex)
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onUserScrollStateChange(true) },
                        onDragEnd = { onUserScrollStateChange(false) },
                        onDragCancel = { onUserScrollStateChange(false) },
                    ) { _, _ -> }
                },
    ) {
        itemsIndexed(
            items = scriptLines,
            key = { _, item -> item.id },
        ) { index, item ->
            val isHighlighted = (item.id == highlightedId)
            val isPast = highlightedIndex >= 0 && index <= highlightedIndex
            ScriptRow(
                item = item,
                isHighlighted = isHighlighted,
                isPast = isPast,
                onClick = { onLineClick(item) },
            )
        }
    }
}
