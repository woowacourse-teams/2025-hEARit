package com.onair.hearit.domain.model

data class RandomHearit(
    val id: Long,
    val title: String,
    val summary: String,
    val source: String,
    val playTime: Int,
    val createdAt: String,
)
