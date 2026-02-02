package com.onair.hearit.admin.ai.infrastructure.llm;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LlmResponse {

    private final String text;
    private final String finishReason;
    private final long latencyMs;
    private final String provider;

    public boolean isComplete() {
        return "STOP".equals(finishReason);
    }

    public boolean isLengthLimited() {
        return "MAX_TOKENS".equals(finishReason) || "LENGTH".equals(finishReason);
    }

    public boolean isSafetyFiltered() {
        return "SAFETY".equals(finishReason);
    }
}
