package com.onair.hearit.domain

data class ScriptLine(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String,
)
