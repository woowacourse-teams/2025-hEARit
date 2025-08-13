package com.onair.hearit.auth.dto;

public class UserContext {

    private final Long memberId;
    private final boolean isAuthenticated;

    private UserContext(Long memberId, boolean isAuthenticated) {
        this.memberId = memberId;
        this.isAuthenticated = isAuthenticated;
    }

    public static UserContext guest() {
        return new UserContext(null, false);
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
