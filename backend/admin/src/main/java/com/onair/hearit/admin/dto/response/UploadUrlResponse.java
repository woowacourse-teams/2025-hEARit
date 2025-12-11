package com.onair.hearit.admin.dto.response;

import java.net.URL;

public record UploadUrlResponse(
        String key,
        URL url
) {
}
