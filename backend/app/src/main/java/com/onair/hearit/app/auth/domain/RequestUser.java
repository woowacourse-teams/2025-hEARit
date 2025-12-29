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

    private final UUID memberUuid;
    private final UUID guestUuid;

    private RequestUser(UUID memberUuid, UUID guestUuid) {
        validate(memberUuid, guestUuid);
        this.memberUuid = memberUuid;
        this.guestUuid = guestUuid;
    }

    public static RequestUser guest(String guestId) {
        if (guestId == null || guestId.isBlank()) {
            log.warn("현재 Device-Uuid Header가 비어있습니다.");
            return new RequestUser(null, FALLBACK_GUEST_UUID);
        }
        return new RequestUser(null, UUID.fromString(guestId));
    }

    public static RequestUser member(UUID memberUuid) {
        if (memberUuid == null) {
            throw new IllegalStateException("memberUuid는 null일 수 없습니다.");
        }
        return new RequestUser(memberUuid, null);
    }

    public String getUserType() {
        if (memberUuid == null) {
            return UserType.GUEST.getName();
        }
        return UserType.MEMBER.getName();
    }

    public String getMemberId() {
        return memberUuid != null ? memberUuid.toString() : null;
    }

    public String getGuestId() {
        return guestUuid != null ? guestUuid.toString() : null;
    }

    public UserInfo getUserInfo() {
        return new UserInfo(memberUuid, guestUuid);
    }

    private void validate(UUID memberUuid, UUID guestUuid) {
        if (memberUuid == null && guestUuid == null) {
            //FIXME: 커스텀 예외
            throw new IllegalStateException("RequestUser를 생성할 수 없습니다.");
        }
    }
}
