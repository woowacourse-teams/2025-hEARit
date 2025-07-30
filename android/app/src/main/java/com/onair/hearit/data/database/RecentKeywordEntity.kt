package com.onair.hearit.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_keyword")
data class RecentKeywordEntity(
    @PrimaryKey val term: String,
    val searchedAt: Long,
)
