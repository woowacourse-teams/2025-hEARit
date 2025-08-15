package com.onair.hearit.auth.application;

import com.onair.hearit.auth.domain.OAuthProvider;
import com.onair.hearit.auth.dto.request.OAuthUserInfo;

public interface OAuthService {

    OAuthProvider provider();

    OAuthUserInfo fetchUser(String accessToken);
}
