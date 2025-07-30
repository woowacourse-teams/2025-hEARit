package com.onair.hearit.data.repository

import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.datasource.KeywordRemoteDataSource
import com.onair.hearit.data.toDomain
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.repository.KeywordRepository

class KeywordRepositoryImpl(
    private val keywordRemoteDataSource: KeywordRemoteDataSource,
    private val crashlyticsLogger: CrashlyticsLogger,
) : KeywordRepository {
    override suspend fun getRecommendKeywords(size: Int?): Result<List<Keyword>> =
        handleResult(crashlyticsLogger) {
            val response = keywordRemoteDataSource.getRecommendKeywords(size).getOrThrow()
            response.map { it.toDomain() }
        }
}
