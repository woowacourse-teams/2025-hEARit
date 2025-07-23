package com.onair.hearit.data.repository

import android.util.Log
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
            val a = hearitLocalDataSource.getRecentHearit().getOrThrow()?.toDomain()
            Log.d("meeple_log", "$a")
            a
        }

    override suspend fun saveRecentHearit(recentHearit: RecentHearit): Result<Unit> =
        handleResult {
            hearitLocalDataSource.saveRecentHearit(recentHearit.toData())
        }
}
