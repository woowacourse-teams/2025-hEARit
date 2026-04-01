package com.onair.hearit.presentation.search.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.presentation.calculateProgress
import com.onair.hearit.presentation.theme.DarkGray
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitPurple1

@Composable
fun HearitProgressBar(
    lastPlayTimeMillis: Long?,
    totalPlayTimeSec: Int,
    progressColor: Color,
    modifier: Modifier = Modifier,
) {
    val progress =
        remember(lastPlayTimeMillis, totalPlayTimeSec) {
            calculateProgress(lastPlayTimeMillis, totalPlayTimeSec)
        }

    CustomLinearProgressBar(
        progress = progress,
        backgroundColor = DarkGray,
        progressColor = progressColor,
        cornerRadius = 48.dp,
        modifier =
            modifier
                .fillMaxWidth()
                .height(4.dp)
                .padding(start = 20.dp, end = 8.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun HearitProgressBarPreview() {
    MaterialTheme {
        Column(modifier = Modifier.background(HearitBlack).padding(16.dp)) {
            HearitProgressBar(
                lastPlayTimeMillis = 99999L,
                totalPlayTimeSec = 350,
                progressColor = HearitPurple1,
            )
        }
    }
}
