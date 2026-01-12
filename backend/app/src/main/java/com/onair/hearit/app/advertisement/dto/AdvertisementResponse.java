package com.onair.hearit.app.advertisement.dto;

import com.onair.hearit.core.domain.Advertisement;

public record AdvertisementResponse(
        Long id,
        String imageUrl,
        String linkUrl,
        String title
) {
    public static AdvertisementResponse from(Advertisement advertisement) {
        return new AdvertisementResponse(
                advertisement.getId(),
                advertisement.getImageUrl(),
                advertisement.getLinkUrl(),
                advertisement.getTitle()
        );
    }
}
