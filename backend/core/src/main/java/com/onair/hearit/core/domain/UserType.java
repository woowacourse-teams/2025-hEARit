package com.onair.hearit.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserType {
    MEMBER("member"),
    GUEST("guest"),
    ;

    private final String name;
}
