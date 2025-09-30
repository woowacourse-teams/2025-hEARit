package com.onair.hearit.app.auth.dto.response;

import com.onair.hearit.core.log.mask.Masking;
import com.onair.hearit.core.log.mask.MaskingType;

public record TokenReissueResponse(
        @Masking(type = MaskingType.TOKEN)
        String accessToken
) {
}
