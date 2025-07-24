package com.onair.hearit.domain.model

data class Paging(
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
)
