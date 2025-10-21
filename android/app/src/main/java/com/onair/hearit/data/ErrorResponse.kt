package com.onair.hearit.data

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String = "",
    val code: String,
    val reissuable: Boolean,
    val properties: ErrorProperties? = null,
)
