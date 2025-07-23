package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.KeywordDataSource
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.repository.KeywordRepository

class KeywordRepositoryImpl(
    private val keywordDataSource: KeywordDataSource,
) : KeywordRepository {
    override suspend fun getRecommendKeywords(size: Int?): Result<List<Keyword>> =
        handleResult {
            val response = keywordDataSource.getRecommendKeywords(size).getOrThrow()
            response.map { it.toDomain() }
        }
}
