package com.onair.hearit.auth.domain;

public class UserContext {

    private final Long memberId;
    private final String guestId;

    private UserContext(Long memberId, String guestId) {
        this.memberId = memberId;
        this.guestId = guestId;
    }

    public static UserContext guest(String guestId) {
        if(guestId == null || guestId.isBlank()) {
            throw new IllegalStateException("guestId는 null이거나 비어있을 수 없습니다.");
        }
        return new UserContext(null, guestId);
    }

    public static UserContext member(Long memberId) {
        if(memberId == null) {
            throw new IllegalStateException("guestId는 null일 수 없습니다.");
        }
        return new UserContext(memberId, null);
    }

    public boolean isMember() {
        return this.memberId != null;
    }

    public boolean isGuest() {
        return this.guestId != null;
    }

    public Long getMemberId() {
        if (isGuest()) {
            throw new IllegalStateException("비회원 컨텍스트에서는 memberId를 가져올 수 없습니다.");
        }
        return this.memberId;
    }

    public String getGuestId() {
        if (isMember()) {
            throw new IllegalStateException("회원 컨텍스트에서는 guestId를 가져올 수 없습니다.");
        }
        return this.guestId;
    }
}
