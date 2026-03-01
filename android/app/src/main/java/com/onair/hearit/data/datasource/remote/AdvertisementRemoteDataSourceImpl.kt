package com.onair.hearit.data.datasource.remote

import com.onair.hearit.data.api.AdvertisementService
import com.onair.hearit.data.datasource.ErrorResponseHandler
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.handleApiCall
import com.onair.hearit.data.dto.AdvertisementResponse
import javax.inject.Inject
import javax.inject.Named

class AdvertisementRemoteDataSourceImpl @Inject constructor(
    @param:Named("noAuth") private val advertisementService: AdvertisementService,
    private val errorResponseHandler: ErrorResponseHandler,
) : AdvertisementRemoteDataSource {
    override suspend fun getAdvertisement(): NetworkResult<AdvertisementResponse> =
        handleApiCall(
            apiCall = { advertisementService.getAdvertisement() },
            errorHandler = errorResponseHandler,
        )
}
