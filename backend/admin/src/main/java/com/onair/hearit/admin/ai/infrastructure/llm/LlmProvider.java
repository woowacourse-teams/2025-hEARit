package com.onair.hearit.admin.ai.infrastructure.llm;

public interface LlmProvider {

    LlmResponse generate(LlmRequest request);

    LlmResponse generateJson(LlmRequest request);

    String getProviderName();

    boolean isAvailable();
}
