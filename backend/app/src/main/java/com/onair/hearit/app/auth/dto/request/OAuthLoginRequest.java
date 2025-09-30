package com.onair.hearit.app.auth.dto.request;

import com.onair.hearit.core.log.mask.Masking;
import com.onair.hearit.core.log.mask.MaskingType;
import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(

        @NotBlank
        @Masking(type = MaskingType.TOKEN)
        String accessToken
) {
}
