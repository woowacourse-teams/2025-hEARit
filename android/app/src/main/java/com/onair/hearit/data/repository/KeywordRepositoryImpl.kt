package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.KeywordRemoteDataSource
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.repository.KeywordRepository

class KeywordRepositoryImpl(
    private val keywordRemoteDataSource: KeywordRemoteDataSource,
) : KeywordRepository {
    override suspend fun getRecommendKeywords(size: Int?): Result<List<Keyword>> =
        handleResult {
            val response = keywordRemoteDataSource.getRecommendKeywords(size).getOrThrow()
            response.map { it.toDomain() }
        }
}
