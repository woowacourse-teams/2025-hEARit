package com.onair.hearit.domain.model

data class CursorResult<T>(
    val items: List<T>,
    val isEmpty: Boolean,
)
