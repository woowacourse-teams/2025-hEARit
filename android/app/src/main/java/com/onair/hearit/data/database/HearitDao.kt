package com.onair.hearit.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HearitDao {
    @Query("SELECT * FROM recent_hearit LIMIT 1")
    suspend fun getRecentHearit(): RecentHearitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentHearit(entity: RecentHearitEntity)
}
