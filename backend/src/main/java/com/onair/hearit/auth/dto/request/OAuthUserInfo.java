package com.onair.hearit.auth.dto.request;

import com.onair.hearit.auth.application.OAuthProvider;

public record OAuthUserInfo(
        String id,
        String nickname,
        String profileImageUrl,
        OAuthProvider provider
) {
}
