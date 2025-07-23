package com.onair.hearit.domain.model

data class HearitShorts(
    val id: Long,
    val title: String,
    val summary: String,
    val source: String,
    val audioUrl: String,
    val script: List<ScriptLine>,
    val playTime: Int,
    val createdAt: String,
)
