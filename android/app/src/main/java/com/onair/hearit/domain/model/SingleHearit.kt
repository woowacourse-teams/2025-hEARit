package com.onair.hearit.domain.model

data class SingleHearit(
    val id: Long,
    val title: String,
    val summary: String,
    val source: String,
    val playTime: Int,
    val createdAt: String,
    val isBookmarked: Boolean,
    val bookmarkId: Long?,
)
