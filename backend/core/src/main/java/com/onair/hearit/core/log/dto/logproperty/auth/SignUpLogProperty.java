package com.onair.hearit.core.log.dto.logproperty.auth;

import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.OAuthProvider;
import com.onair.hearit.core.log.dto.LogEvent;
import com.onair.hearit.core.log.dto.logproperty.LogProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignUpLogProperty implements LogProperty {

    private final String signupProvider;
    private final String createdAt;
    private final long memberId;

    public static SignUpLogProperty ofOAuth(Member member, OAuthProvider provider) {
        return new SignUpLogProperty(provider.getName(), member.getCreatedAt().toString(), member.getId());
    }

    public static SignUpLogProperty fromLocal(Member member) {
        return new SignUpLogProperty(OAuthProvider.NONE.getName(), member.getCreatedAt().toString(),
                member.getId());
    }

    @Override
    public String getEventName() {
        return LogEvent.SIGNUP.getEventName();
    }
}
