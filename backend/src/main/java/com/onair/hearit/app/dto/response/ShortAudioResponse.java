package com.onair.hearit.app.dto.response;

import com.onair.hearit.log.mask.Masking;
import com.onair.hearit.log.mask.MaskingType;

public record ShortAudioResponse(
        Long id,
        @Masking(type = MaskingType.URL) String url
) {
}
