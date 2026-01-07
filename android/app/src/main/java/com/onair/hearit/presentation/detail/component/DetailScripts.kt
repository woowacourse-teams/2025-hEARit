package com.onair.hearit.presentation.detail.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.onair.hearit.domain.model.ScriptLine

@Composable
fun DetailScripts(
    scriptLines: List<ScriptLine>,
    highlightedId: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val highlightedIndex: Int =
        remember(scriptLines, highlightedId) {
            scriptLines.indexOfFirst { it.id == highlightedId }
        }
    val scrollState = rememberDetailScriptScrollState(scriptLines)

    AutoCenterOnHighlight(
        scrollState = scrollState,
        highlightedIndex = highlightedIndex,
        totalCount = scriptLines.size,
    )

    Box(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        LazyColumn(
            state = scrollState.listState,
            userScrollEnabled = false,
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(
                items = scriptLines,
                key = { _, item -> item.id },
            ) { _, item ->
                DetailScriptRow(
                    item = item,
                    isHighlighted = (highlightedId == item.id),
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .blockAllGesturesExceptTap(onTap = onClick),
        )
    }
}

private fun Modifier.blockAllGesturesExceptTap(onTap: () -> Unit): Modifier =
    pointerInput(onTap) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)

            val up = waitForUpOrCancellation()
            if (up != null) onTap()
        }
    }
