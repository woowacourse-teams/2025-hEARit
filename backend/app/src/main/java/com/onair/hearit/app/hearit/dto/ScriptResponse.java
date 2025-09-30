package com.onair.hearit.app.hearit.dto;

import com.onair.hearit.core.log.mask.Masking;
import com.onair.hearit.core.log.mask.MaskingType;

public record ScriptResponse(
        Long id,
        @Masking(type = MaskingType.URL) String url
) {
}
