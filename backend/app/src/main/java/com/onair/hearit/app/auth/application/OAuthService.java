package com.onair.hearit.app.auth.application;

import com.onair.hearit.app.auth.dto.response.OAuthUserInfoResponse;
import com.onair.hearit.core.domain.OAuthProvider;

public interface OAuthService {

    OAuthProvider provider();

    OAuthUserInfoResponse fetchUser(String accessToken);
}
