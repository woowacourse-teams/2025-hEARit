package com.onair.hearit.data

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String = "",
    val instance: String? = null,
    val properties: ErrorProperties? = null,
)
