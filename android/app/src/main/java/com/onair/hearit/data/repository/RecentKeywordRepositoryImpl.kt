package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.toData
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.RecentSearch
import com.onair.hearit.domain.repository.RecentKeywordRepository

class RecentKeywordRepositoryImpl(
    private val hearitLocalDataSource: HearitLocalDataSource,
) : RecentKeywordRepository {
    override suspend fun getKeywords(): Result<List<RecentSearch>> =
        handleResult {
            hearitLocalDataSource.getKeywords().getOrThrow().map { it.toDomain() }
        }

    override suspend fun saveKeyword(keyword: String): Result<Unit> =
        handleResult {
            val timestamp = System.currentTimeMillis()
            val recentSearch = RecentSearch(term = keyword, searchedAt = timestamp)
            hearitLocalDataSource.saveKeyword(recentSearch.toData())
        }

    override suspend fun clearKeywords(): Result<Unit> =
        handleResult {
            hearitLocalDataSource.clearKeywords()
        }
}
