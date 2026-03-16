package com.onair.hearit.domain.repository

import com.onair.hearit.domain.model.Advertisement

interface AdvertisementRepository {
    suspend fun getAdvertisement(): Result<Advertisement>
}
