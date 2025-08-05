package com.onair.hearit.domain.model

data class RandomHearit(
    val id: Long,
    val title: String,
    val categoryColorCode: String,
    val isBookmarked: Boolean,
    val bookmarkId: Long?,
    val keywords: List<Keyword>,
)
