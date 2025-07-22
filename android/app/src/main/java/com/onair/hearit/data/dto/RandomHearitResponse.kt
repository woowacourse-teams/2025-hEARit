package com.onair.hearit.data.dto

import com.onair.hearit.domain.RandomHearitItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RandomHearitResponse(
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

fun RandomHearitResponse.toDomain(): RandomHearitItem =
    RandomHearitItem(
        id = this.id,
        title = this.title,
        summary = this.summary,
        source = this.source,
        playTime = this.playTime,
        createdAt = this.createdAt,
    )
