package com.onair.hearit.admin.ai.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "llm")
@Validated
@Getter
@Setter
public class LlmProviderProperties {

    @NotBlank
    private String activeProvider = "gemini";

    private GeminiProperties gemini = new GeminiProperties();
    private RateLimitProperties rateLimit = new RateLimitProperties();
    private RetryProperties retry = new RetryProperties();
    private MetadataProperties metadata = new MetadataProperties();

    @Getter
    @Setter
    public static class GeminiProperties {
        @NotBlank
        private String apiKey;
        private String model = "gemini-2.5-flash";
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        private double temperature = 0.7;
        private int maxOutputTokens = 65536;
    }

    @Getter
    @Setter
    public static class RateLimitProperties {
        @Positive
        private long minIntervalMs = 4000;
    }

    @Getter
    @Setter
    public static class RetryProperties {
        @Positive
        private int maxRetries = 3;
        @Positive
        private long initialDelayMs = 5000;
        private double backoffMultiplier = 2.0;
        @Positive
        private long maxDelayMs = 60000;
    }

    @Getter
    @Setter
    public static class MetadataProperties {
        @Positive
        private int maxScriptLength = 10000;
        @Positive
        private int maxTitleLength = 35;
        @Positive
        private int maxSummaryLength = 250;
    }
}
