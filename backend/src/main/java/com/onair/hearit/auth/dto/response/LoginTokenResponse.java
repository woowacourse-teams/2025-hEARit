package com.onair.hearit.auth.dto.response;

import com.onair.hearit.common.log.mask.Masking;
import com.onair.hearit.common.log.mask.MaskingType;

public record LoginTokenResponse(

        @Masking(type = MaskingType.TOKEN)
        String accessToken,

        @Masking(type = MaskingType.TOKEN)
        String refreshToken
) {
}
