package com.onair.hearit.dto.response;

import com.onair.hearit.common.log.mask.Masking;
import com.onair.hearit.common.log.mask.MaskingType;

public record ShortAudioResponse(
        Long id,
        @Masking(type = MaskingType.URL) String url
) {
}
