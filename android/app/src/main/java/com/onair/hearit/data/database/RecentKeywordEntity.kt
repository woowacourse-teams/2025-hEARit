package com.onair.hearit.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_keyword")
data class RecentKeywordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val term: String,
    val searchedAt: String,
)
