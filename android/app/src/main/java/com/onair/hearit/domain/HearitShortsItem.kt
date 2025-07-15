package com.onair.hearit.domain

import android.net.Uri
import com.onair.hearit.domain.SubtitleLine
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
