package com.onair.hearit.data

class HearitRemoteDataSourceImpl(
    private val hearitService: HearitService,
) : HearitRemoteDataSource {
    override suspend fun getRecommendHearits(): Result<List<RecommendHearitResponse>> =
        handleApiCall(
            errorMessage = "추천 히어릿 조회 실패",
            apiCall = { hearitService.getRecommendHearits() },
            transform = { response ->
                response.body() ?: throw IllegalStateException("응답 바디가 null입니다.")
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
}
