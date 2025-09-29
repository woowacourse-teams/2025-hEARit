package com.onair.hearit.common;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import org.springframework.data.auditing.DateTimeProvider;

/**
 * A controllable DateTimeProvider for tests.
 * This allows JPA Auditing to use a fixed time that we can set dynamically within a test.
 */
public class TestClock implements DateTimeProvider {

    private static LocalDateTime fixedTime;

    /**
     * Freezes the clock at a specific timestamp.
     */
    public static void freezeAt(LocalDateTime time) {
        fixedTime = time;
    }

    /**
     * Resets the clock to use the current system time.
     */
    public static void unfreeze() {
        fixedTime = null;
    }

    @Override
    public Optional<TemporalAccessor> getNow() {
        LocalDateTime now = (fixedTime != null) ? fixedTime : LocalDateTime.now();
        return Optional.of(now);
    }
}
