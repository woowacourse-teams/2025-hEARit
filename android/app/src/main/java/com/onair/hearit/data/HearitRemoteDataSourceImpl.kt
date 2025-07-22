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
}
