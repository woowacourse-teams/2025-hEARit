package com.onair.hearit.data.datasource.local

import com.onair.hearit.data.database.RecentHearitEntity
import com.onair.hearit.data.database.RecentKeywordEntity

interface HearitLocalDataSource {
    suspend fun getRecentHearit(): Result<RecentHearitEntity?>

    suspend fun saveRecentHearit(entity: RecentHearitEntity): Result<Unit>

    suspend fun getKeywords(): Result<List<RecentKeywordEntity>>

    suspend fun saveKeyword(keyword: RecentKeywordEntity): Result<Unit>

    suspend fun clearKeywords(): Result<Unit>
}
