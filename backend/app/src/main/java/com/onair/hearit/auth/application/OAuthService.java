package com.onair.hearit.auth.application;

import com.onair.hearit.domain.OAuthProvider;
import com.onair.hearit.auth.dto.response.OAuthUserInfoResponse;

public interface OAuthService {

    OAuthProvider provider();

    OAuthUserInfoResponse fetchUser(String accessToken);
}
