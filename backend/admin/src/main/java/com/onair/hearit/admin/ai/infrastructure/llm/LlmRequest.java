package com.onair.hearit.admin.ai.infrastructure.llm;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LlmRequest {

    private final String prompt;
    private final Double temperature;
    private final Integer maxOutputTokens;

    public static LlmRequest of(String prompt) {
        return LlmRequest.builder()
                .prompt(prompt)
                .build();
    }

    public static LlmRequest of(String prompt, Double temperature) {
        return LlmRequest.builder()
                .prompt(prompt)
                .temperature(temperature)
                .build();
    }
}
