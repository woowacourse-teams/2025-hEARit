package com.onair.hearit.core.log.property.auth;

import com.onair.hearit.core.log.LogEvent;
import com.onair.hearit.core.log.property.LogProperty;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenRefreshLogProperty implements LogProperty {

    private final UUID memberId;
    private final String refreshedAt;
    private final boolean success;
    private final String failureReason;

    public static TokenRefreshLogProperty success(UUID memberId) {
        return new TokenRefreshLogProperty(memberId, LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString(),
                true, null);
    }

    public static TokenRefreshLogProperty failure(UUID memberId, String failureReason) {
        return new TokenRefreshLogProperty(memberId, LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString(),
                false, failureReason);
    }

    @Override
    public String getEventName() {
        return LogEvent.TOKEN_REFRESH.getEventName();
    }

}
