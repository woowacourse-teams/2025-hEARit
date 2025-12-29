package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.UserInfoDomainException;
import java.util.UUID;

public class UserInfo {

    private final UUID memberUuid;
    private final UUID guestUuid;
    private final UserType userType;

    public UserInfo(UUID memberUuid, UUID guestUuid) {
        validate(memberUuid, guestUuid);
        this.memberUuid = memberUuid;
        this.guestUuid = guestUuid;
        this.userType = (memberUuid != null) ? UserType.MEMBER : UserType.GUEST;
    }

    private void validate(UUID memberUuid, UUID guestUuid) {
        if (memberUuid == null && guestUuid == null) {
            throw new UserInfoDomainException("UserInfo를 생성할 수 없습니다.");
        }

        if (memberUuid != null && guestUuid != null) {
            throw new UserInfoDomainException("memberUuid와 guestUuid는 동시에 지정할 수 없습니다.");
        }
    }

    public boolean isMember() {
        return this.userType == UserType.MEMBER;
    }

    public boolean isGuest() {
        return this.userType == UserType.GUEST;
    }

    public UUID getMemberUuid() {
        if (isGuest()) {
            throw new IllegalStateException("비회원 컨텍스트에서는 memberUuid를 가져올 수 없습니다.");
        }
        return this.memberUuid;
    }

    public UUID getGuestUuid() {
        if (isMember()) {
            throw new IllegalStateException("회원 컨텍스트에서는 guestUuid를 가져올 수 없습니다.");
        }
        return this.guestUuid;
    }

    public UUID getUuid() {
        return isMember() ? memberUuid : guestUuid;
    }

    public UserType getUserType() {
        return this.userType;
    }
}
