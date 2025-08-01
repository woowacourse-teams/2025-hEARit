package com.onair.hearit.data.repository

import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.toData
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.RecentHearit
import com.onair.hearit.domain.repository.RecentHearitRepository

class RecentHearitRepositoryImpl(
    private val hearitLocalDataSource: HearitLocalDataSource,
    private val crashlyticsLogger: CrashlyticsLogger,
) : RecentHearitRepository {
    override suspend fun getRecentHearit(): Result<RecentHearit?> =
        handleResult(crashlyticsLogger) {
            hearitLocalDataSource.getRecentHearit().getOrThrow()?.toDomain()
        }

    override suspend fun saveRecentHearit(recentHearit: RecentHearit): Result<Unit> =
        handleResult(crashlyticsLogger) {
            hearitLocalDataSource.saveRecentHearit(recentHearit.toData())
        }

    override suspend fun updateRecentHearitPosition(
        hearitId: Long,
        position: Long,
    ): Result<Unit> =
        handleResult(crashlyticsLogger) {
            hearitLocalDataSource.updateRecentHearitPosition(hearitId, position)
        }
}
