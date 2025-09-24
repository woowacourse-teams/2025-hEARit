package com.onair.hearit.domain.model

// RoomDB에 저장하는 가장 최근 들은 히어릿 1개
data class RecentHearit(
    val id: Long,
    val title: String,
    val lastPosition: Long? = null,
)
