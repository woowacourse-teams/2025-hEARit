package com.onair.hearit.data

import com.onair.hearit.domain.RecommendHearitItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendHearitResponse(
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("id")
    val id: Long,
    @SerialName("playTime")
    val playTime: Int,
    @SerialName("source")
    val source: String,
    @SerialName("summary")
    val summary: String,
    @SerialName("title")
    val title: String,
)

fun RecommendHearitResponse.toDomain(): RecommendHearitItem =
    RecommendHearitItem(
        id = this.id,
        title = this.title,
        desc = this.summary,
    )
