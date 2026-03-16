package com.onair.hearit.data.mapper

import com.onair.hearit.data.dto.AdvertisementResponse
import com.onair.hearit.domain.model.Advertisement

fun AdvertisementResponse.toDomain(): Advertisement =
    Advertisement(
        id = id,
        imageUrl = imageUrl,
        linkUrl = linkUrl,
        title = title,
    )
