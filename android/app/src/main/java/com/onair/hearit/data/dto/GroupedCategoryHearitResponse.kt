package com.onair.hearit.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupedCategoryHearitResponse(
    @SerialName("categoryId")
    val categoryId: Long,
    @SerialName("categoryName")
    val categoryName: String,
    @SerialName("colorCode")
    val colorCode: String,
    @SerialName("hearits")
    val categoryHearitResponses: List<CategoryHearitResponse>,
)
