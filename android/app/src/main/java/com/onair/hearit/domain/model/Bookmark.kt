package com.onair.hearit.domain.model

data class Bookmark(
    val hearitId: Long,
    val bookmarkId: Long,
    val title: String,
    val summary: String,
    val playTime: Int,
    val lastPlayTime: Long? = null,
    val isFinished: Boolean? = null,
    val sources: List<Source>,
    val category: Category,
    val audioUrl: String?,
)
