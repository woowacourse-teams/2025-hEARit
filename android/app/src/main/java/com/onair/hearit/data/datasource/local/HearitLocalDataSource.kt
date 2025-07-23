package com.onair.hearit.data.datasource.local

import com.onair.hearit.data.database.RecentHearitEntity

interface HearitLocalDataSource {
    suspend fun getRecentHearit(): Result<RecentHearitEntity?>

    suspend fun saveRecentHearit(entity: RecentHearitEntity): Result<Unit>
}
