package com.onair.hearit.data

import retrofit2.Response
import retrofit2.http.GET

interface HearitService {
    @GET("hearits/recommend")
    suspend fun getRecommendHearits(): Response<List<RecommendHearitResponse>>
}
