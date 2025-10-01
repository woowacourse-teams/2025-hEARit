package com.onair.hearit.domain.model

data class RecentUploadHearit(
    val id: Int,
    val title: String,
    val playTime: Int,
    val lastPlayTime: Long = 0L,
    val category: Category,
)
