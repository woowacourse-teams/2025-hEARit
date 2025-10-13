package com.onair.hearit.domain.model

data class RecentUploadHearit(
    val id: Long,
    val title: String,
    val playTime: Int,
    val lastPlayTime: Long? = null,
    val createdAt: String,
    val keywords: List<Keyword>,
    val category: Category,
)
