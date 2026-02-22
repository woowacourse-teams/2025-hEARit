package com.onair.hearit.presentation.explore.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import kotlinx.collections.immutable.ImmutableList

/**
 * 현재 재생 위치에 맞춰 스크립트 목록을 표시하는 컴포넌트입니다.
 * 현재 스크립트를 중심으로 이전/이후 스크립트를 함께 보여주며 하이라이트 효과를 줍니다.
 *
 * @param scripts 표시할 전체 스크립트 리스트
 * @param currentPosition 현재 오디오 재생 위치 (ms)
 */
@Composable
fun ColumnScope.ScriptContent(
    scripts: ImmutableList<ScriptLine>,
    currentPosition: Long,
    modifier: Modifier = Modifier,
) {
    val currentScriptIndex =
        remember(currentPosition) {
            val index = scripts.binarySearch { it.start.compareTo(currentPosition) }
            if (index < 0) (-(index + 1) - 1).coerceAtLeast(0) else index
        }
    val displayRange =
        remember(currentScriptIndex, scripts.size) {
            if (scripts.isEmpty()) return@remember IntRange.EMPTY
            val start = (currentScriptIndex - 1).coerceAtLeast(0)
            val end = (start + 2).coerceAtMost(scripts.lastIndex)
            val finalStart = (end - 2).coerceAtLeast(0)
            finalStart..end
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            userScrollEnabled = false,
        ) {
            items(
                count = if (displayRange.isEmpty()) 0 else (displayRange.last - displayRange.first + 1),
                key = { index ->
                    // 3. Key 안정성 확보: 단순히 index가 아닌 데이터의 고유값(start 시간 등)을 키로 사용
                    scripts[displayRange.first + index].start
                },
            ) { index ->
                val actualIndex = displayRange.first + index
                val scriptLine = scripts[actualIndex]
                val isCurrent = actualIndex == currentScriptIndex

                ScriptItem(
                    text = scriptLine.text,
                    isCurrent = isCurrent,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun ScriptItem(
    text: String,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = if (isCurrent) Gray4 else Gray2,
        style = if (isCurrent) HearitTypoGraphy.bodyLarge else HearitTypoGraphy.titleSmall,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .padding(vertical = 14.dp)
                .fillMaxWidth(),
    )
}
