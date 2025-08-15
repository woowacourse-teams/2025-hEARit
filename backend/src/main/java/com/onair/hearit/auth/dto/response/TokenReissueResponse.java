package com.onair.hearit.auth.dto.response;

import com.onair.hearit.common.log.mask.Masking;
import com.onair.hearit.common.log.mask.MaskingType;

public record TokenReissueResponse(
        @Masking(type = MaskingType.TOKEN)
        String accessToken
) {
}
