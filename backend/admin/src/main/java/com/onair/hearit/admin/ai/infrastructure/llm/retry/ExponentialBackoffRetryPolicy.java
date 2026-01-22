package com.onair.hearit.admin.ai.infrastructure.llm.retry;

import com.onair.hearit.admin.ai.exception.LlmApiException;
import com.onair.hearit.admin.ai.exception.LlmRateLimitException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
public class ExponentialBackoffRetryPolicy implements RetryPolicy {

    private final int maxRetries;
    private final long initialDelayMs;
    private final double backoffMultiplier;
    private final long maxDelayMs;

    public ExponentialBackoffRetryPolicy(
            int maxRetries,
            long initialDelayMs,
            double backoffMultiplier,
            long maxDelayMs) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.maxDelayMs = maxDelayMs;
    }

    @Override
    public <T> T execute(Supplier<T> operation) {
        int retryCount = 0;
        long currentDelay = initialDelayMs;

        while (true) {
            try {
                return operation.get();
            } catch (Exception e) {
                if (!isRetryable(e) || retryCount >= maxRetries) {
                    log.error("재시도 불가 또는 최대 재시도 횟수 초과: retryCount={}, maxRetries={}",
                            retryCount, maxRetries, e);
                    throw wrapException(e);
                }

                retryCount++;
                log.warn("재시도 예정: {}/{}회, 대기 {}ms, 오류: {}",
                        retryCount, maxRetries, currentDelay, e.getMessage());

                sleep(currentDelay);
                currentDelay = calculateNextDelay(currentDelay);
            }
        }
    }

    @Override
    public boolean isRetryable(Exception e) {
        if (e instanceof LlmRateLimitException) {
            return true;
        }

        if (e instanceof HttpClientErrorException httpEx) {
            return httpEx.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
        }

        if (e instanceof HttpServerErrorException) {
            return true;
        }

        if (e instanceof ResourceAccessException) {
            return true;
        }

        return false;
    }

    private long calculateNextDelay(long currentDelay) {
        long nextDelay = (long) (currentDelay * backoffMultiplier);
        return Math.min(nextDelay, maxDelayMs);
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmApiException("재시도 중 인터럽트 발생", e);
        }
    }

    private RuntimeException wrapException(Exception e) {
        if (e instanceof RuntimeException runtimeEx) {
            return runtimeEx;
        }
        return new LlmApiException("LLM API 호출 실패", e);
    }
}
