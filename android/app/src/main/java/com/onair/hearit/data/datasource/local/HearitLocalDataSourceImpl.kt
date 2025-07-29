package com.onair.hearit.data.datasource.local

import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.database.HearitDao
import com.onair.hearit.data.database.RecentHearitEntity
import com.onair.hearit.data.repository.handleResult

class HearitLocalDataSourceImpl(
    private val hearitDao: HearitDao,
    private val crashlyticsLogger: CrashlyticsLogger,
) : HearitLocalDataSource {
    override suspend fun getRecentHearit(): Result<RecentHearitEntity?> = handleResult(crashlyticsLogger) { hearitDao.getRecentHearit() }

    override suspend fun saveRecentHearit(entity: RecentHearitEntity): Result<Unit> =
        handleResult(crashlyticsLogger) { hearitDao.insertRecentHearit(entity) }
}
