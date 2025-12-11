package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.mapper.toData
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.RecentHearitRepository
import javax.inject.Inject

class RecentHearitRepositoryImpl @Inject constructor(
    private val hearitLocalDataSource: HearitLocalDataSource,
) : RecentHearitRepository {
    override suspend fun getRecentHearit(): Result<RecentHearit?> = hearitLocalDataSource.getRecentHearit().mapCatching { it?.toDomain() }

    override suspend fun saveRecentHearit(recentHearit: RecentHearit): Result<Unit> =
        hearitLocalDataSource.saveRecentHearit(recentHearit.toData())

    override suspend fun updateRecentHearitPosition(
        hearitId: Long,
        position: Long,
    ): Result<Unit> = hearitLocalDataSource.updateRecentHearitPosition(hearitId, position)
}
