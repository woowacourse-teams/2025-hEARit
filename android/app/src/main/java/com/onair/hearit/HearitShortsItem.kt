package com.onair.hearit

import android.net.Uri
import java.time.LocalDateTime

data class HearitShortsItem(
    val id: Long,
    val title: String,
    val summary: String,
    val audioUri: Uri,
    val script: List<SubtitleLine>,
    val playTime: Int,
    val categoryId: Int,
    val createdAt: LocalDateTime,
)
