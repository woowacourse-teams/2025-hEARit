package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.dto.AdvertisementResponse

interface AdvertisementRemoteDataSource {
    suspend fun getAdvertisement(): NetworkResult<AdvertisementResponse>
}
