package com.onair.hearit.presentation.explore.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.HearitPurple2
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * 오디오 재생 진행 상태를 표시하고 사용자가 탐색할 수 있는 프로그레스 바
 *
 * 이 컴포저블은 현재 재생 위치와 전체 길이를 바탕으로 진행률을 시각화하며,
 * 사용자의 터치(탭) 및 드래그 제스처를 통해 재생 위치를 변경할 수 있는 기능을 제공합니다.
 *
 * @param currentPositionMs 현재 오디오의 재생 위치 (밀리초 단위)
 * @param durationMs 오디오의 전체 길이 (밀리초 단위)
 * @param onPositionChanged 사용자가 프로그레스 바를 클릭하거나 드래그하여 위치를 변경했을 때 호출되는 콜백 함수.
 */
@Composable
fun AudioProgressBar(
    currentPositionMs: Long,
    durationMs: Long,
    onPositionChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragPositionMs by remember { mutableStateOf<Long?>(null) }

    val displayPosition = dragPositionMs ?: currentPositionMs
    val progress =
        if (durationMs > 0) (displayPosition.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    fun calculateNewTime(
        offsetX: Float,
        width: Int,
    ): Long {
        val newPosition = (offsetX / width).coerceIn(0f, 1f)
        return (newPosition * durationMs).toLong()
    }

    Box(
        modifier =
            modifier
                .height(4.dp)
                .fillMaxWidth()
                .background(color = Gray2)
                .pointerInput(durationMs) {
                    coroutineScope {
                        launch {
                            detectTapGestures { offset ->
                                onPositionChanged(calculateNewTime(offset.x, size.width))
                            }
                        }
                        launch {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    dragPositionMs = calculateNewTime(offset.x, size.width)
                                },
                                onDragEnd = {
                                    dragPositionMs?.let { onPositionChanged(it) }
                                    dragPositionMs = null
                                },
                                onDragCancel = {
                                    dragPositionMs = null
                                },
                                onHorizontalDrag = { change, _ ->
                                    change.consume()
                                    dragPositionMs = calculateNewTime(change.position.x, size.width)
                                },
                            )
                        }
                    }
                },
    ) {
        Box(
            modifier =
                Modifier
                    .height(4.dp)
                    .fillMaxWidth()
                    .background(color = Gray2),
        )
        Box(
            modifier =
                Modifier
                    .height(4.dp)
                    .fillMaxWidth(progress)
                    .background(color = HearitPurple2),
        )
    }
}
