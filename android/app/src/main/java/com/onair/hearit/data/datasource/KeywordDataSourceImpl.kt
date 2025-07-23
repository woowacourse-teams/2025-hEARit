package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.KeywordService
import com.onair.hearit.data.dto.KeywordResponse

class KeywordDataSourceImpl(
    private val keywordService: KeywordService,
) : KeywordDataSource {
    override suspend fun getRecommendKeywords(size: Int?): Result<List<KeywordResponse>> =
        handleApiCall(
            errorMessage = "키워드 조회 실패",
            apiCall = { keywordService.getRecommendKeywords(size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
            },
        )
}
