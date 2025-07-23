package com.onair.hearit.domain

fun RandomHearit.toHearitShortsItem(
    audioUrl: String,
    script: List<ScriptLine>,
): ShortsHearit =
    ShortsHearit(
        id = this.id,
        title = this.title,
        summary = this.summary,
        source = this.source,
        audioUrl = audioUrl,
        script = script,
        playTime = this.playTime,
        createdAt = this.createdAt,
    )
