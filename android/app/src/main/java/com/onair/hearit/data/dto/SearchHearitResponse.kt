package com.onair.hearit.data.dto

import com.onair.hearit.domain.SearchedHearitItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchHearitResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("playTime")
    val playTime: Int,
    @SerialName("summary")
    val summary: String,
    @SerialName("title")
    val title: String,
)

fun SearchHearitResponse.toDomain(): SearchedHearitItem =
    SearchedHearitItem(
        id = this.id,
        title = this.title,
        playTime = this.playTime,
        summary = this.summary,
    )
