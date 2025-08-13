package com.onair.hearit.auth.dto.request;

import com.onair.hearit.common.log.mask.Masking;
import com.onair.hearit.common.log.mask.MaskingType;

public record SignupRequest(

        String localId,

        String nickname,

        @Masking(type = MaskingType.FULL)
        String password
) {
}
