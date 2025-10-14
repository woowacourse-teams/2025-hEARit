package com.onair.hearit.core.log.property.auth;

import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.log.LogEvent;
import com.onair.hearit.core.log.property.LogProperty;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WithdrawalLogProperty implements LogProperty {

    private final long memberId;
    private final int membershipDays;

    public static WithdrawalLogProperty from(Member member) {
        int membershipDays = getMembershipDays(member);
        return new WithdrawalLogProperty(member.getId(), membershipDays);
    }

    private static int getMembershipDays(Member member) {
        LocalDateTime createdAt = member.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        return (int) ChronoUnit.DAYS.between(createdAt, now);
    }

    @Override
    public String getEventName() {
        return LogEvent.WITHDRAWAL.getEventName();
    }
}
