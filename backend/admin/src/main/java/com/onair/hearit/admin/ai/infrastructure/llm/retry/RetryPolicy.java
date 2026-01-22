package com.onair.hearit.admin.ai.infrastructure.llm.retry;

import java.util.function.Supplier;

public interface RetryPolicy {

    <T> T execute(Supplier<T> operation);

    boolean isRetryable(Exception e);
}
