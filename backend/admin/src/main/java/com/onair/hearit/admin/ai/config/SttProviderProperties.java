package com.onair.hearit.admin.ai.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "stt")
@Validated
@Getter
@Setter
public class SttProviderProperties {

    @NotBlank
    private String activeProvider = "groq";

    private GroqProperties groq = new GroqProperties();
    private RateLimitProperties rateLimit = new RateLimitProperties();
    private RetryProperties retry = new RetryProperties();

    @Getter
    @Setter
    public static class GroqProperties {
        @NotBlank
        private String apiKey;
        private String model = "whisper-large-v3-turbo";
        private String baseUrl = "https://api.groq.com/openai/v1/audio/transcriptions";
        private String language = "ko";
        @Positive
        private int maxFileSizeBytes = 25 * 1024 * 1024; // 25MB
    }

    @Getter
    @Setter
    public static class RateLimitProperties {
        @Positive
        private long minIntervalMs = 1000;
    }

    @Getter
    @Setter
    public static class RetryProperties {
        @Positive
        private int maxRetries = 3;
        @Positive
        private long initialDelayMs = 2000;
        private double backoffMultiplier = 2.0;
        @Positive
        private long maxDelayMs = 30000;
    }
}
