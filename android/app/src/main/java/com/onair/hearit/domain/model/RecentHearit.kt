package com.onair.hearit.domain.model

data class RecentHearit(
    val id: Long,
    val title: String,
    val lastPosition: Long = 0L,
)
