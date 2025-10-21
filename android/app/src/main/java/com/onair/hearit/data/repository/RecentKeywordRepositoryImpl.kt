package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.local.HearitLocalDataSource
import com.onair.hearit.data.mapper.toData
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.RecentSearch
import com.onair.hearit.domain.repository.RecentKeywordRepository
import javax.inject.Inject

class RecentKeywordRepositoryImpl @Inject constructor(
    private val hearitLocalDataSource: HearitLocalDataSource,
) : RecentKeywordRepository {
    override suspend fun getKeywords(): Result<List<RecentSearch>> =
        hearitLocalDataSource.getKeywords().mapCatching { list -> list.map { it.toDomain() } }

    override suspend fun saveKeyword(keyword: String): Result<Unit> {
        val timestamp = System.currentTimeMillis()
        val recentSearch = RecentSearch(term = keyword, searchedAt = timestamp)
        return hearitLocalDataSource.saveKeyword(recentSearch.toData())
    }

    override suspend fun clearKeywords(): Result<Int> = hearitLocalDataSource.clearKeywords()
}
