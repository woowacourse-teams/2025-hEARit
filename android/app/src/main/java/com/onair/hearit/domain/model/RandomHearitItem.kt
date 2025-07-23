package com.onair.hearit.domain.model

data class RandomHearitItem(
    val id: Long,
    val title: String,
    val summary: String,
    val source: String,
    val playTime: Int,
    val createdAt: String,
)
