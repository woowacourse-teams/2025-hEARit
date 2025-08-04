package com.onair.hearit.data.datasource.local

import com.onair.hearit.data.database.HearitDao
import com.onair.hearit.data.database.RecentHearitEntity
import com.onair.hearit.data.database.SearchHistoryEntity

class HearitLocalDataSourceImpl(
    private val hearitDao: HearitDao,
) : HearitLocalDataSource {
    override suspend fun getRecentHearit(): Result<RecentHearitEntity?> = runCatching { hearitDao.getRecentHearit() }

    override suspend fun saveRecentHearit(entity: RecentHearitEntity): Result<Unit> = runCatching { hearitDao.insertRecentHearit(entity) }

    override suspend fun getKeywords(): Result<List<SearchHistoryEntity>> = runCatching { hearitDao.getKeywords() }

    override suspend fun saveKeyword(keyword: SearchHistoryEntity): Result<Unit> = runCatching { hearitDao.insertKeyword(keyword) }

    override suspend fun clearKeywords(): Result<Unit> = runCatching { hearitDao.deleteKeywords() }

    override suspend fun updateRecentHearitPosition(
        hearitId: Long,
        position: Long,
    ): Result<Unit> = runCatching { hearitDao.updateLastPosition(hearitId, position) }
}
