package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KeywordResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
)
