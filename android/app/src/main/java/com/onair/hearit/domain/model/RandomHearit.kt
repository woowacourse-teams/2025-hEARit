package com.onair.hearit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RandomHearit(
    val id: Long,
    val title: String,
    val summary: String,
    val source: String,
    val playTime: Int,
    val createdAt: String,
    val isBookmarked: Boolean,
    val bookmarkId: Long?,
)
