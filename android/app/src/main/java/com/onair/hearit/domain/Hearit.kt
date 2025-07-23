package com.onair.hearit.domain

import com.onair.hearit.domain.model.ScriptLine

data class Hearit(
    val id: Long,
    val title: String,
    val summary: String,
    val source: String,
    val audioUrl: String,
    val script: List<ScriptLine>,
    val playTime: Int,
    val createdAt: String,
    val isBookmarked: Boolean,
    val bookmarkId: Int?,
)
