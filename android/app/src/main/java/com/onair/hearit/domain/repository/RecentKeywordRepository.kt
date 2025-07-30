package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.RecentSearch

interface RecentKeywordRepository {
    suspend fun getKeywords(): Result<List<RecentSearch>>

    suspend fun saveKeyword(keyword: String): Result<Unit>

    suspend fun clearKeywords(): Result<Unit>
}
