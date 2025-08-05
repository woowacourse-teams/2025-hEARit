package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.KeywordRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.repository.KeywordRepository

class KeywordRepositoryImpl(
    private val keywordRemoteDataSource: KeywordRemoteDataSource,
) : KeywordRepository {
    override suspend fun getRecommendKeywords(size: Int?): Result<List<Keyword>> =
        keywordRemoteDataSource.getRecommendKeywords(size).mapListOrThrowDomain { it.toDomain() }
}
