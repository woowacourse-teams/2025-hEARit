package com.onair.hearit.data.repository

import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.toData
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.RecentKeyword
import com.onair.hearit.domain.repository.RecentKeywordRepository

class RecentKeywordRepositoryImpl(
    private val hearitLocalDataSource: HearitLocalDataSource,
    private val crashlyticsLogger: CrashlyticsLogger,
) : RecentKeywordRepository {
    override suspend fun getKeywords(): Result<List<RecentKeyword>> =
        handleResult(crashlyticsLogger) {
            hearitLocalDataSource.getKeywords().getOrThrow().map { it.toDomain() }
        }

    override suspend fun saveKeyword(keyword: String): Result<Unit> =
        handleResult(crashlyticsLogger) {
            val timestamp = System.currentTimeMillis()
            val recentKeyword = RecentKeyword(term = keyword, searchedAt = timestamp)
            hearitLocalDataSource.saveKeyword(recentKeyword.toData())
        }

    override suspend fun clearKeywords(): Result<Unit> =
        handleResult(crashlyticsLogger) {
            hearitLocalDataSource.clearKeywords()
        }
}
