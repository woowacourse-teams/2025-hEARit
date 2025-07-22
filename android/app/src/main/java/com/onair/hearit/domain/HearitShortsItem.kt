package com.onair.hearit.domain

data class HearitShortsItem(
    val id: Long,
    val title: String,
    val summary: String,
    val source: String,
    val audioUrl: String,
    val script: List<SubtitleLine>,
    val playTime: Int,
    val createdAt: String,
)
