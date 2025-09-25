package com.onair.hearit.hearit.dto;

import com.onair.hearit.log.mask.Masking;
import com.onair.hearit.log.mask.MaskingType;

public record ScriptResponse(
        Long id,
        @Masking(type = MaskingType.URL) String url
) {
}
