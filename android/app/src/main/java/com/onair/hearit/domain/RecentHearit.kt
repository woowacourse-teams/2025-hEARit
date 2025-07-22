package com.onair.hearit.domain

import java.time.LocalDateTime

data class RecentHearit(
    val id: Long,
    val title: String,
    val summary: String,
    val audioUrl: String,
    val scriptUrl: String,
    val playTime: Int,
    val categoryId: Int,
    val createdAt: LocalDateTime,
)
