package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.KeywordService
import com.onair.hearit.data.dto.KeywordResponse

class KeywordRemoteDataSourceImpl(
    private val keywordService: KeywordService,
    private val errorResponseHandler: ErrorResponseHandler,
) : KeywordRemoteDataSource {
    override suspend fun getRecommendKeywords(size: Int?): Result<NetworkResult<List<KeywordResponse>>> =
        handleApiCall(
            apiCall = { keywordService.getRecommendKeywords(size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
            },
            errorHandler = errorResponseHandler,
        )
}
