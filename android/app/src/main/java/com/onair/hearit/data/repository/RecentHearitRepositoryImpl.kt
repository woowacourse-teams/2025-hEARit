package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.toData
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.RecentHearitRepository

class RecentHearitRepositoryImpl(
    private val hearitLocalDataSource: HearitLocalDataSource,
) : RecentHearitRepository {
    override suspend fun getRecentHearit(): Result<RecentHearit?> =
        handleResult {
            hearitLocalDataSource.getRecentHearit().getOrThrow()?.toDomain()
        }

    override suspend fun saveRecentHearit(recentHearit: RecentHearit): Result<Unit> =
        handleResult {
            hearitLocalDataSource.saveRecentHearit(recentHearit.toData())
        }
}
