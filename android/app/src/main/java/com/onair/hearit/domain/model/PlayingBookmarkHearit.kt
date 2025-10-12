package com.onair.hearit.domain.model

// 북마크 중 듣고 있는 히어릿
data class PlayingBookmarkHearit(
    val hearitId: Long,
    val bookmarkId: Long,
    val title: String,
    val playTime: Int,
    val lastPlayTime: Long = 0L,
    val category: Category,
)
