package com.onair.hearit.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 상단에서 하단으로 페이드되는 그라디언트 생성
 */
@Composable
fun rememberTopFadeGradient(
    topColor: Color,
    bottomColor: Color,
    fadeStop: Float = 0.2f,
): Brush =
    remember(topColor, bottomColor, fadeStop) {
        Brush.verticalGradient(
            colorStops =
                arrayOf(
                    0.0f to topColor,
                    fadeStop.coerceIn(0f, 1f) to bottomColor,
                ),
        )
    }
