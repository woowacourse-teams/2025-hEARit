package com.onair.hearit.domain

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
