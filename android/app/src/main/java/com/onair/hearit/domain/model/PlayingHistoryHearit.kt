package com.onair.hearit.domain.model

// 서버에서 받아오는 최근 들은 히어릿
data class PlayingHistoryHearit(
    val id: Long,
    val title: String,
    val playTime: Int,
    val lastPlayTime: Long = 0L,
    val createdAt: String,
    val category: Category,
)
