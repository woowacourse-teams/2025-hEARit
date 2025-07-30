package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.RecentKeyword

interface RecentKeywordRepository {
    suspend fun getKeywords(): Result<List<RecentKeyword>>

    suspend fun saveKeyword(keyword: RecentKeyword): Result<Unit>

    suspend fun clearKeywords(): Result<Unit>
}
