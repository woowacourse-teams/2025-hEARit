package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.KeywordResponse

interface KeywordRemoteDataSource {
    suspend fun getRecommendKeywords(size: Int?): Result<List<KeywordResponse>>
}
