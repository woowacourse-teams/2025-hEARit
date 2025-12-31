package com.onair.hearit.presentation.detail.script.component

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.presentation.detail.component.rememberScriptScrollState

@Composable
fun Scripts(
    scriptLines: List<ScriptLine>,
    highlightedId: Long?,
    isUserScrolling: Boolean,
    followHighlight: Boolean,
    onLineClick: (ScriptLine) -> Unit,
    onUserScrollStateChange: (Boolean) -> Unit,
    onStopFollow: () -> Unit,
) {
    val scrollState = rememberScriptScrollState { scriptLines }

    val highlightedIndex: Int =
        remember(scriptLines, highlightedId) {
            if (highlightedId == null) {
                -1
            } else {
                scriptLines.indexOfFirst { it.id == highlightedId }
            }
        }

    LaunchedEffect(scrollState.listState) {
        snapshotFlow { scrollState.listState.isScrollInProgress }.collect { inProgress ->
            onUserScrollStateChange(inProgress)
            if (inProgress && !scrollState.isAuto) {
                onStopFollow()
            }
        }
    }

    LaunchedEffect(highlightedIndex, scriptLines.size, followHighlight) {
        if (!followHighlight) return@LaunchedEffect
        if (isUserScrolling || scrollState.isAuto) return@LaunchedEffect
        if (highlightedIndex !in scriptLines.indices) return@LaunchedEffect
        if (scrollState.lastCentered == highlightedIndex) return@LaunchedEffect
        scrollState.centerVariableHeight(highlightedIndex)
    }

    LazyColumn(
        state = scrollState.listState,
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            onUserScrollStateChange(true)
                            onStopFollow()
                        },
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
