package com.onair.hearit.domain

import kotlinx.serialization.Serializable

@Serializable
data class ScriptLine(
    val id: Long,
    val start: Long,
    val end: Long,
    val text: String,
)
