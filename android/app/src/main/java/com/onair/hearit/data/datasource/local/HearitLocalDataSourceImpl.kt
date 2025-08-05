package com.onair.hearit.data.datasource.local

import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.database.HearitDao
import com.onair.hearit.data.database.RecentHearitEntity
import com.onair.hearit.data.database.SearchHistoryEntity
import com.onair.hearit.data.repository.handleResult

class HearitLocalDataSourceImpl(
    private val hearitDao: HearitDao,
    private val crashlyticsLogger: CrashlyticsLogger,
) : HearitLocalDataSource {
    override suspend fun getRecentHearit(): Result<RecentHearitEntity?> = handleResult(crashlyticsLogger) { hearitDao.getRecentHearit() }

    override suspend fun saveRecentHearit(entity: RecentHearitEntity): Result<Unit> =
        handleResult(crashlyticsLogger) { hearitDao.insertRecentHearit(entity) }

    override suspend fun getKeywords(): Result<List<SearchHistoryEntity>> = handleResult(crashlyticsLogger) { hearitDao.getKeywords() }

    override suspend fun saveKeyword(keyword: SearchHistoryEntity): Result<Unit> =
        handleResult(crashlyticsLogger) { hearitDao.insertKeyword(keyword) }

    override suspend fun clearKeywords(): Result<Unit> = handleResult(crashlyticsLogger) { hearitDao.deleteKeywords() }

    override suspend fun updateRecentHearitPosition(
        hearitId: Long,
        position: Long,
    ): Result<Unit> = handleResult(crashlyticsLogger) { hearitDao.updateLastPosition(hearitId, position) }
}
