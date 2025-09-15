package com.onair.hearit.domain.model

data class SearchedHearit(
    val id: Long,
    val title: String,
    val playTime: Int,
    val lastPlayTime: Long? = null,
    val keywords: List<Keyword>,
)
