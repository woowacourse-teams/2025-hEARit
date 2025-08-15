package com.onair.hearit.auth.dto.request;

import com.onair.hearit.common.log.mask.Masking;
import com.onair.hearit.common.log.mask.MaskingType;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(

        @NotBlank
        @Masking(type = MaskingType.EDGE)
        String localId,

        @NotBlank
        @Masking(type = MaskingType.FULL)
        String nickname,

        @NotBlank
        @Masking(type = MaskingType.FULL)
        String password
) {
}
