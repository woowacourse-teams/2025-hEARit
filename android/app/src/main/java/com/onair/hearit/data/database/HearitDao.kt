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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyword(keyword: SearchHistoryEntity)

    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC")
    suspend fun getKeywords(): List<SearchHistoryEntity>

    @Query("DELETE FROM search_history")
    suspend fun deleteKeywords()
}
