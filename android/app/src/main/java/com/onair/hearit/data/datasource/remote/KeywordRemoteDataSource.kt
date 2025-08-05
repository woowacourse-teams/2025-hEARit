package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.KeywordResponse

interface KeywordRemoteDataSource {
    suspend fun getRecommendKeywords(size: Int?): Result<NetworkResult<List<KeywordResponse>>>
}
