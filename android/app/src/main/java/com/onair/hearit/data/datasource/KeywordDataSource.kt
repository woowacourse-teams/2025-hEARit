package com.onair.hearit.data.datasource

import com.onair.hearit.data.dto.KeywordResponse

interface KeywordDataSource {
    suspend fun getRecommendKeywords(size: Int?): Result<List<KeywordResponse>>
}
