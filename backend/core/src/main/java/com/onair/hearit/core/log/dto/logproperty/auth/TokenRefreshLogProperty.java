package com.onair.hearit.core.log.dto.logproperty.auth;

import com.onair.hearit.core.log.dto.LogEvent;
import com.onair.hearit.core.log.dto.logproperty.LogProperty;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenRefreshLogProperty implements LogProperty {

    private final long memberId;
    private final String refreshedAt;
    private final boolean success;
    private final String failureReason;

    public static TokenRefreshLogProperty success(long memberId) {
        return new TokenRefreshLogProperty(memberId, LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString(),
                true, "null");
    }

    public static TokenRefreshLogProperty failure(long memberId, String failureReason) {
        return new TokenRefreshLogProperty(memberId, LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString(),
                false, failureReason);
    }

    @Override
    public String getEventName() {
        return LogEvent.TOKEN_REFRESH.getEventName();
    }

}
