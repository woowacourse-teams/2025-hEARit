package com.onair.hearit.domain.model

data class Bookmark(
    val hearitId: Long,
    val bookmarkId: Long,
    val title: String,
    val summary: String,
    val playTime: Int,
)
