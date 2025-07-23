package com.onair.hearit.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_hearit")
data class RecentHearitEntity(
    @PrimaryKey val id: Int = 1,
    val hearitId: Long,
    val title: String,
)
