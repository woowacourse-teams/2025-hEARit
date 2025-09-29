package com.onair.hearit.app.auth.dto.request;

import com.onair.hearit.core.log.mask.Masking;
import com.onair.hearit.core.log.mask.MaskingType;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        @Masking(type = MaskingType.EDGE)
        String localId,

        @NotBlank
        @Masking(type = MaskingType.FULL)
        String password
) {
}
