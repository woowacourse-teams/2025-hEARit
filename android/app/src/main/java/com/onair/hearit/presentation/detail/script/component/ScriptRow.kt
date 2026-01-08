package com.onair.hearit.presentation.detail.script.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.presentation.theme.DarkGray
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.util.noRippleClickable

@Composable
fun ScriptRow(
    item: ScriptLine,
    isHighlighted: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetTextColor: Color =
        when {
            isHighlighted || isPast -> Gray4
            else -> DarkGray
        }
    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        label = "scriptRowTextColor",
    )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .noRippleClickable(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = item.text,
            color = animatedTextColor,
            overflow = TextOverflow.Ellipsis,
            style = HearitTypoGraphy.titleMedium,
        )
    }
}
