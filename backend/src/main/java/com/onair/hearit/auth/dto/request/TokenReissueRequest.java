package com.onair.hearit.auth.dto.request;

import com.onair.hearit.common.log.mask.Masking;
import com.onair.hearit.common.log.mask.MaskingType;
import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(

        @NotBlank
        @Masking(type = MaskingType.TOKEN)
        String refreshToken
) {
}
