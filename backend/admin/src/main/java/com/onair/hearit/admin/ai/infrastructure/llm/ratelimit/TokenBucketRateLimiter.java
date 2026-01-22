package com.onair.hearit.admin.ai.infrastructure.llm.ratelimit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenBucketRateLimiter {

    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition rateLimitCondition = lock.newCondition();
    private final long minIntervalMs;
    private volatile long lastRequestTime = 0;

    public TokenBucketRateLimiter(long minIntervalMs) {
        this.minIntervalMs = minIntervalMs;
    }

    public void acquire() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            waitForRateLimit();
            lastRequestTime = System.currentTimeMillis();
        } finally {
            lock.unlock();
        }
    }

    private void waitForRateLimit() throws InterruptedException {
        while (true) {
            long elapsed = System.currentTimeMillis() - lastRequestTime;
            long waitTime = minIntervalMs - elapsed;

            if (waitTime <= 0 || lastRequestTime == 0) {
                return;
            }

            log.debug("Rate limit 대기: {}ms", waitTime);
            rateLimitCondition.await(waitTime, TimeUnit.MILLISECONDS);
        }
    }
}
