package com.onair.hearit.presentation.explore.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        remember(currentPosition, scripts) {
            scripts.indexOfLast { it.start <= currentPosition }.coerceAtLeast(0)
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
            val start = (currentScriptIndex - 1).coerceAtLeast(0)
            val end = (start + 2).coerceAtMost(scripts.lastIndex)

            val finalStart = (end - 2).coerceAtLeast(0)
            val displayIndices = finalStart..end

            items(
                items = displayIndices.toList(),
                key = { it },
            ) { index ->
                val isCurrent = index == currentScriptIndex
                val scriptLine = scripts[index]

                Text(
                    text = scriptLine.text,
                    color = if (isCurrent) Gray4 else Gray2,
                    style = if (isCurrent) HearitTypoGraphy.bodyLarge else HearitTypoGraphy.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .padding(vertical = 14.dp)
                            .fillMaxWidth()
                            .animateItem(),
                )
            }
        }
    }
}
