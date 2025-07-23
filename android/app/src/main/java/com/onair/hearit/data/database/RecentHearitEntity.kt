package com.onair.hearit.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_hearit")
data class RecentHearitEntity(
    @PrimaryKey @ColumnInfo(name = "id") val hearitId: Long,
    @ColumnInfo(name = "title") val title: String,
)
