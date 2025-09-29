package com.onair.hearit.app.auth.dto.response;

import com.onair.hearit.core.domain.OAuthProvider;

public record OAuthUserInfoResponse(
        String id,
        String nickname,
        String profileImageUrl,
        OAuthProvider provider
) {
}
