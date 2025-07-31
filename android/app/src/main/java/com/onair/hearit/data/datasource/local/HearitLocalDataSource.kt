package com.onair.hearit.data.datasource.local

import com.onair.hearit.data.database.RecentHearitEntity
import com.onair.hearit.data.database.SearchHistoryEntity

interface HearitLocalDataSource {
    suspend fun getRecentHearit(): Result<RecentHearitEntity?>

    suspend fun saveRecentHearit(entity: RecentHearitEntity): Result<Unit>

    suspend fun getKeywords(): Result<List<SearchHistoryEntity>>

    suspend fun saveKeyword(keyword: SearchHistoryEntity): Result<Unit>

    suspend fun clearKeywords(): Result<Unit>
}
