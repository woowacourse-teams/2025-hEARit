package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourceResponse(
    @SerialName("sourceName")
    val sourceName: String,
    @SerialName("sourceUrl")
    val sourceUrl: String?,
)
