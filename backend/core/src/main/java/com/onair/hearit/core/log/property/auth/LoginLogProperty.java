package com.onair.hearit.core.log.property.auth;

import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.OAuthProvider;
import com.onair.hearit.core.log.LogEvent;
import com.onair.hearit.core.log.property.LogProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginLogProperty implements LogProperty {

    private final String loginProvider;
    private final long memberId;

    public static LoginLogProperty successLocal(Member member) {
        return new LoginLogProperty(OAuthProvider.NONE.getName(), member.getId());
    }

    public static LoginLogProperty successOAuth(Member member, OAuthProvider provider) {
        return new LoginLogProperty(provider.getName(), member.getId());
    }

    @Override
    public String getEventName() {
        return LogEvent.LOGIN.getEventName();
    }
}
