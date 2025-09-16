package com.onair.hearit.domain.model

data class SingleHearit(
    val id: Long,
    val title: String,
    val summary: String,
    val sources: List<Source>,
    val playTime: Int,
    val lastPlayTime: Long? = null,
    val createdAt: String,
    val isBookmarked: Boolean,
    val bookmarkId: Long?,
    val category: Category,
    val keywords: List<Keyword>,
)
