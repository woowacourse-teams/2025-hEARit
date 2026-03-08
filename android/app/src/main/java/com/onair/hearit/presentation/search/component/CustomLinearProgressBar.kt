package com.onair.hearit.presentation.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Composable
fun CustomLinearProgressBar(
    progress: Float,
    backgroundColor: Color,
    progressColor: Color,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    val clamped = progress.coerceIn(0f, 1f)

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clamped)
                    .background(progressColor),
        )
    }
}
