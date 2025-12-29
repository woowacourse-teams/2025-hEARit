package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.UserInfoDomainException;
import java.util.UUID;

public class UserInfo {

    private final UUID uuid;
    private final UserType userType;

    public UserInfo(UUID uuid, UserType userType) {
        validate(uuid, userType);
        this.uuid = uuid;
        this.userType = userType;
    }

    private void validate(UUID uuid, UserType userType) {
        if (uuid == null) {
            throw new UserInfoDomainException("uuid는 null일 수 없습니다.");
        }
        if (userType == null) {
            throw new UserInfoDomainException("userType은 null일 수 없습니다.");
        }
    }

    public boolean isMember() {
        return this.userType == UserType.MEMBER;
    }

    public boolean isGuest() {
        return this.userType == UserType.GUEST;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public UserType getUserType() {
        return this.userType;
    }
}
