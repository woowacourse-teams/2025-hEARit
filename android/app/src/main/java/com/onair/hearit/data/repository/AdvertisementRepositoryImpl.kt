package com.onair.hearit.data.repository

import com.onair.hearit.data.datasource.remote.AdvertisementRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.data.toDomainResult
import com.onair.hearit.domain.model.Advertisement
import com.onair.hearit.domain.repository.AdvertisementRepository
import javax.inject.Inject

class AdvertisementRepositoryImpl @Inject constructor(
    private val advertisementRemoteDataSource: AdvertisementRemoteDataSource,
) : AdvertisementRepository {
    override suspend fun getAdvertisement(): Result<Advertisement> =
        advertisementRemoteDataSource.getAdvertisement().toDomainResult { it.toDomain() }
}
