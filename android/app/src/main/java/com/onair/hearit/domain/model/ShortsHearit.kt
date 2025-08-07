package com.onair.hearit.domain.model

data class ShortsHearit(
    val id: Long,
    val title: String,
    val audioUrl: String,
    val script: List<ScriptLine>,
    val isBookmarked: Boolean,
    val bookmarkId: Long?,
    val keywords: List<Keyword>,
    val categoryColorCode: String,
)
