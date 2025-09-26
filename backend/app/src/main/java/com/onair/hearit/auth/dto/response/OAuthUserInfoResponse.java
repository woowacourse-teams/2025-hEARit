package com.onair.hearit.auth.dto.response;

import com.onair.hearit.domain.OAuthProvider;

public record OAuthUserInfoResponse(
        String id,
        String nickname,
        String profileImageUrl,
        OAuthProvider provider
) {
}
