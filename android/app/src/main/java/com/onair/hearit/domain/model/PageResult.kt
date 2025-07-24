package com.onair.hearit.domain.model

data class PageResult<T>(
    val items: List<T>,
    val paging: Paging,
)
