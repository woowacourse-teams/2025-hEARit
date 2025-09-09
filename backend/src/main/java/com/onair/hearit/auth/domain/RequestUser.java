package com.onair.hearit.auth.domain;

import com.onair.hearit.common.domain.UserInfo;

public class RequestUser {

    private final Long memberId;
    private final String guestId;

    private RequestUser(Long memberId, String guestId) {
        validate(memberId, guestId);
        this.memberId = memberId;
        this.guestId = guestId;
    }

    public static RequestUser guest(String guestId) {
        if (guestId == null || guestId.isBlank()) {
            throw new IllegalStateException("guestId는 null이거나 비어있을 수 없습니다.");
        }
        return new RequestUser(null, guestId);
    }

    public static RequestUser member(Long memberId) {
        if (memberId == null) {
            throw new IllegalStateException("guestId는 null일 수 없습니다.");
        }
        return new RequestUser(memberId, null);
    }

    private void validate(Long memberId, String guestId) {
        if (memberId == null && guestId == null) {
            //FIXME: 커스텀 예외
            throw new IllegalStateException("UserContext를 생성할 수 없습니다.");
        }
        if (guestId != null) {
            validateGuestId(guestId);
        }
    }

    private void validateGuestId(String guestId) {
        if (guestId == null || guestId.length() != 36) {
            //FIXME: 커스텀 예외
            throw new IllegalStateException("유효하지 않은 guestId입니다.");
        }
    }

    public UserInfo getUserInfo() {
        return new UserInfo(memberId, guestId);
    }
}
