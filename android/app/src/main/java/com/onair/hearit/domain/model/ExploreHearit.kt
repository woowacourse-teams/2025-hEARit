package com.onair.hearit.domain.model

data class ExploreHearit(
    val id: Long,
    val title: String,
    val categoryColorCode: String,
    val isBookmarked: Boolean,
    val bookmarkId: Long?,
    val keywords: List<Keyword>,
    val cursorId: Long,
    val audioUrl: String?,
    val script: List<ScriptLine>?,
)
