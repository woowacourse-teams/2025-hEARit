package com.onair.hearit.data

import kotlinx.serialization.Serializable

@Serializable
data class ErrorProperties(
    val code: String,
    val reissuable: Boolean,
)
