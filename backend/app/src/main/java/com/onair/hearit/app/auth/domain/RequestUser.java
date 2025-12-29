package com.onair.hearit.app.auth.domain;

import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.domain.UserType;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Getter
@Log4j2
public class RequestUser {

    private static final UUID FALLBACK_GUEST_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final UUID uuid;
    private final UserType userType;

    private RequestUser(UUID uuid, UserType userType) {
        validate(uuid, userType);
        this.uuid = uuid;
        this.userType = userType;
    }

    public static RequestUser guest(String guestId) {
        if (guestId == null || guestId.isBlank()) {
            log.warn("현재 Device-Uuid Header가 비어있습니다.");
            return new RequestUser(FALLBACK_GUEST_UUID, UserType.GUEST);
        }
        return new RequestUser(UUID.fromString(guestId), UserType.GUEST);
    }

    public static RequestUser member(UUID memberUuid) {
        return new RequestUser(memberUuid, UserType.MEMBER);
    }

    public String getUserType() {
        return userType.getName();
    }

    public UserInfo getUserInfo() {
        return new UserInfo(uuid, userType);
    }

    private void validate(UUID uuid, UserType userType) {
        if (userType == null) {
            throw new IllegalStateException("userType은 null일 수 없습니다.");
        }
    }
}
