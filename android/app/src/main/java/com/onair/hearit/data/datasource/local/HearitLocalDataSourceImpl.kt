package com.onair.hearit.data.datasource.local

import com.onair.hearit.data.database.HearitDao
import com.onair.hearit.data.database.RecentHearitEntity
import com.onair.hearit.data.repository.handleResult

class HearitLocalDataSourceImpl(
    private val hearitDao: HearitDao,
) : HearitLocalDataSource {
    override suspend fun getRecentHearit(): Result<RecentHearitEntity?> = handleResult { hearitDao.getRecentHearit() }

    override suspend fun saveRecentHearit(entity: RecentHearitEntity): Result<Unit> = handleResult { hearitDao.insertRecentHearit(entity) }
}
