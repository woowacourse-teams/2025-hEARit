package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.KeywordService
import com.onair.hearit.data.datasource.ApiErrorMessages.ERROR_RESPONSE_BODY_NULL_MESSAGE
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.KeywordResponse

class KeywordRemoteDataSourceImpl(
    private val keywordService: KeywordService,
    private val errorResponseHandler: ErrorResponseHandler,
) : KeywordRemoteDataSource {
    override suspend fun getRecommendKeywords(size: Int?): Result<NetworkResult<List<KeywordResponse>>> =
        handleApiCall(
            apiCall = { keywordService.getRecommendKeywords(size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
            errorHandler = errorResponseHandler,
        )
}
