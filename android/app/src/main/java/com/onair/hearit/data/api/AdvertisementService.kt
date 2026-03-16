package com.onair.hearit.data.api

import com.onair.hearit.data.dto.AdvertisementResponse
import retrofit2.Response
import retrofit2.http.GET

interface AdvertisementService {
    @GET("api/v1/advertisements/random")
    suspend fun getAdvertisement(): Response<AdvertisementResponse>
}
