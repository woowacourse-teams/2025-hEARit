package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    @SerialName("colorCode")
    val colorCode: String,
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
)
