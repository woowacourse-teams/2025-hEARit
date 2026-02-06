package com.onair.hearit.domain.model

import kotlinx.collections.immutable.ImmutableList

data class SearchedHearit(
    val id: Long,
    val title: String,
    val playTime: Int,
    val lastPlayTime: Long? = null,
    val createdAt: String? = null,
    val keywords: ImmutableList<Keyword>,
    val category: Category? = null,
)
