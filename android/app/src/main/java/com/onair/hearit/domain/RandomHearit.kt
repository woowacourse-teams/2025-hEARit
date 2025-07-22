package com.onair.hearit.domain

data class RandomHearitItem(
    val id: Long,
    val title: String,
    val summary: String,
    val source: String,
    val playTime: Int,
    val createdAt: String,
)

fun RandomHearitItem.toHearitShortsItem(
    audioUrl: String,
    script: List<ScriptLine>,
): HearitShorts =
    HearitShorts(
        id = this.id,
        title = this.title,
        summary = this.summary,
        source = this.source,
        audioUrl = audioUrl,
        script = script,
        playTime = this.playTime,
        createdAt = this.createdAt,
    )
