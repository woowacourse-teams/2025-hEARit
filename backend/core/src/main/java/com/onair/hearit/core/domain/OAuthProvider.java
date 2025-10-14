package com.onair.hearit.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthProvider {

    NONE("local"),
    KAKAO("kakao"),
    GOOGLE("google"),
    ;

    private final String name;
}
