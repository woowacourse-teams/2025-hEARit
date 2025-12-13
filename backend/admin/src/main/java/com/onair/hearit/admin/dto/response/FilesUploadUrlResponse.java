package com.onair.hearit.admin.dto.response;

public record FilesUploadUrlResponse(
        UploadUrlResponse originalAudio,
        UploadUrlResponse shortAudio,
        UploadUrlResponse script
) {

}
