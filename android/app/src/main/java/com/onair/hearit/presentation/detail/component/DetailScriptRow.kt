package com.onair.hearit.presentation.detail.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.presentation.theme.DarkGray
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitTypoGraphy

@Composable
fun DetailScriptRow(
    item: ScriptLine,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val targetTextColor: Color =
        when {
            isHighlighted -> Gray4
            else -> DarkGray
        }
    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        label = "ScriptRowTextColor",
    )
    val animatedFontSize by animateFloatAsState(
        targetValue = if (isHighlighted) 16f else 14f,
        label = "scriptFontSize",
    )

    Text(
        text = item.text,
        color = animatedTextColor,
        style =
            HearitTypoGraphy.titleMedium.copy(
                fontSize = animatedFontSize.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            ),
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        maxLines = Int.MAX_VALUE,
    )
}
