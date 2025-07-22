package com.onair.hearit.data

class HearitRemoteDataSourceImpl(
    private val hearitService: HearitService,
) : HearitRemoteDataSource {
    override suspend fun getRecommendHearits(): Result<List<RecommendHearitResponse>> =
        handleApiCall(
            errorMessage = ERROR_RECOMMEND_HEARIT_MESSAGE,
            apiCall = { hearitService.getRecommendHearits() },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun getRandomHearits(): Result<List<RandomHearitResponse>> =
        handleApiCall(
            errorMessage = ERROR_RANDOM_HEARIT_MESSAGE,
            apiCall = { hearitService.getRandomHearits() },
            transform = { response ->
                response.body() ?: throw IllegalStateException(ERROR_RESPONSE_BODY_NULL_MESSAGE)
            },
        )

    override suspend fun getSearchHearits(searchTerm: String): Result<List<SearchHearitResponse>> =
        handleApiCall(
            errorMessage = "검색 히어릿 조회 실패",
            apiCall = { hearitService.getSearchHearits(searchTerm) },
            transform = { response ->
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
            },
        )

    companion object {
        private const val ERROR_RECOMMEND_HEARIT_MESSAGE = "추천 히어릿 조회 실패"
        private const val ERROR_RANDOM_HEARIT_MESSAGE = "랜덤 히어릿 조회 실패"
        private const val ERROR_RESPONSE_BODY_NULL_MESSAGE = "응답 바디가 null입니다."
    }
}
