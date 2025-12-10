package com.onair.hearit.admin.dto.response;

public record FilesPresignedUrlResponse(
        PresignedUrlResponse originalAudio,
        PresignedUrlResponse shortAudio,
        PresignedUrlResponse script
) {

}
