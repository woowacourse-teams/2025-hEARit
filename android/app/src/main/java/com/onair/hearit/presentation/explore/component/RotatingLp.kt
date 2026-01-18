package com.onair.hearit.presentation.explore.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.onair.hearit.R
import com.onair.hearit.domain.model.ExploreHearit

/**
 * 회전하는 LP 이미지 컴포넌트
 *
 * [isPlaying] 상태에 따라 LP 이미지가 회전하거나 정지하며, 회전 애니메이션은
 * 선형 이징(Linear Easing)을 사용하여 부드럽고 일정한 속도로 동작합니다.
 *
 * @param item LP의 배경색 정보를 포함하고 있는 탐색 아이템 데이터 모델
 * @param isPlaying 현재 오디오 재생 상태. true일 경우 LP 이미지가 2.4초마다 한 바퀴씩 회전하며, false일 경우 회전을 멈추고 정위치(0도)에 고정됨
 * @param modifier 레이아웃 수정을 위한 [Modifier]. 기본적으로 LP 이미지의 크기 및 회전 효과에 적용됩니다.
 */
@Composable
fun RotatingLp(
    item: ExploreHearit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(2400, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
            )
        } else {
            rotation.stop()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val categoryColor =
            remember(item.categoryColorCode) {
                try {
                    Color(item.categoryColorCode.toColorInt())
                } catch (e: Exception) {
                    Color.Gray
                }
            }
        Spacer(
            modifier =
                Modifier
                    .size(100.dp)
                    .background(
                        color = categoryColor,
                        shape = CircleShape,
                    ),
        )
        Image(
            painter = painterResource(id = R.drawable.img_explore_lp),
            contentDescription = null,
            modifier =
                modifier
                    .graphicsLayer {
                        rotationZ = rotation.value
                    }
                    .size(300.dp),
        )
    }
}
