package com.onair.hearit

import java.time.LocalDateTime

data class HearitItem(
    val id: Long,
    val title: String,
    val summary: String,
    val audioUrl: String,
    val scriptUrl: String,
    val playTime: Int,
    val categoryId: Int,
    val createdAt: LocalDateTime,
)
