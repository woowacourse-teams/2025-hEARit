package com.onair.hearit.auth.domain;

public class UserContext {

    private final Long memberId;
    private final String guestId;
    private final UserType userType;

    private UserContext(Long memberId, String guestId) {
        validate(memberId, guestId);
        this.memberId = memberId;
        this.guestId = guestId;
        this.userType = (memberId != null) ? UserType.MEMBER : UserType.GUEST;
    }

    private void validate(Long memberId, String guestId) {
        if (memberId == null && guestId == null) {
            //FIXME: 커스텀 예외
            throw new IllegalStateException("UserContext를 생성할 수 없습니다.");
        }
        if(guestId != null) {
            validateGuestId(guestId);
        }
    }

    private void validateGuestId(String guestId) {
        if(guestId == null || guestId.length() != 36) {
            //FIXME: 커스텀 예외
            throw new IllegalStateException("유효하지 않은 guestId입니다.");
        }
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
        return this.userType == UserType.MEMBER;
    }

    public boolean isGuest() {
        return this.userType == UserType.GUEST;
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

    public UserType getUserType() {
        return this.userType;
    }
}
