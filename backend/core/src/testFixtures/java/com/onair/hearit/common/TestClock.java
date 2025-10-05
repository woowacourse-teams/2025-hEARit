package com.onair.hearit.common;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import org.springframework.data.auditing.DateTimeProvider;

public class TestClock implements DateTimeProvider {

    private static LocalDateTime fixedTime;

    public static void freezeAt(LocalDateTime time) {
        fixedTime = time;
    }

    public static void unfreeze() {
        fixedTime = null;
    }

    @Override
    public Optional<TemporalAccessor> getNow() {
        LocalDateTime now = (fixedTime == null) ? LocalDateTime.now() : fixedTime;
        return Optional.of(now);
    }
}
