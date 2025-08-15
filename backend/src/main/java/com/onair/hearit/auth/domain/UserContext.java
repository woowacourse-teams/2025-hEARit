package com.onair.hearit.auth.domain;

import java.util.Objects;

public class UserContext {

    private final Long memberId;
    private final boolean isAuthenticated;

    private UserContext(Long memberId, boolean isAuthenticated) {
        this.memberId = Objects.requireNonNull(memberId, "memberId는 null일 수 없습니다.");
        this.isAuthenticated = isAuthenticated;
    }

    public static UserContext guest() {
        return new UserContext(-1L, false);
    }

    public static UserContext member(Long memberId) {
        return new UserContext(memberId, true);
    }

    public boolean isMember() {
        return isAuthenticated;
    }

    public boolean isGuest() {
        return !isAuthenticated;
    }

    public Long memberId() {
        return this.memberId;
    }
}
