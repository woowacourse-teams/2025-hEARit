package com.onair.hearit.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val term: String,
    val searchedAt: Long,
)
