package com.onair.hearit.data.datasource

import com.onair.hearit.data.api.HearitService
import com.onair.hearit.data.dto.HearitResponse
import com.onair.hearit.data.dto.RandomHearitResponse
import com.onair.hearit.data.dto.RecommendHearitResponse
import com.onair.hearit.data.dto.SearchHearitResponse
import com.onair.hearit.di.TokenProvider

class HearitRemoteDataSourceImpl(
    private val hearitService: HearitService,
) : HearitRemoteDataSource {
    override suspend fun getHearit(hearitId: Long): Result<HearitResponse> =
        handleApiCall(
            errorMessage = ERROR_HEARIT_MESSAGE,
            apiCall = { hearitService.getHearit(getAuthHeader(), hearitId) },
            transform = { response ->
                response.body() ?: throw java.lang.IllegalStateException(
                    ERROR_RESPONSE_BODY_NULL_MESSAGE,
                )
            },
        )

    override suspend fun getRecommendHearits(): Result<List<RecommendHearitResponse>> =
        handleApiCall(
            errorMessage = ERROR_RECOMMEND_HEARIT_MESSAGE,
            apiCall = { hearitService.getRecommendHearits() },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun getRandomHearits(
        page: Int?,
        size: Int?,
    ): Result<List<RandomHearitResponse>> =
        handleApiCall(
            errorMessage = ERROR_RANDOM_HEARIT_MESSAGE,
            apiCall = { hearitService.getRandomHearits(page, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun getSearchHearits(
        searchTerm: String,
        page: Int?,
        size: Int?,
    ): Result<List<SearchHearitResponse>> =
        handleApiCall(
            errorMessage = "검색 히어릿 조회 실패",
            apiCall = { hearitService.getSearchHearits(searchTerm, page, size) },
            transform = { response ->
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
            },
        )

    private fun getAuthHeader(): String {
        val token = requireNotNull(TokenProvider.accessToken) { ERROR_TOKEN_NOT_FOUND_MESSAGE }
        return TOKEN.format(token)
    }

    companion object {
        private const val ERROR_HEARIT_MESSAGE = "히어릿 조회 실패"
        private const val ERROR_RECOMMEND_HEARIT_MESSAGE = "추천 히어릿 조회 실패"
        private const val ERROR_RANDOM_HEARIT_MESSAGE = "랜덤 히어릿 조회 실패"
        private const val ERROR_RESPONSE_BODY_NULL_MESSAGE = "응답 바디가 null입니다."
        private const val ERROR_TOKEN_NOT_FOUND_MESSAGE = "토큰이 존재하지 않음"
        private const val TOKEN = "Bearer %s"
    }
}
