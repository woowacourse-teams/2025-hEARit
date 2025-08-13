package com.onair.hearit.auth.dto.request;

import com.onair.hearit.common.log.mask.Masking;
import com.onair.hearit.common.log.mask.MaskingType;

public record KakaoLoginRequest(
        @Masking(type = MaskingType.TOKEN)
        String accessToken
) {
}
