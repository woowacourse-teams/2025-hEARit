package com.onair.hearit.admin.dto.response;

import java.net.URL;

public record PresignedUrlResponse(
        String key,
        URL url
) {
}
