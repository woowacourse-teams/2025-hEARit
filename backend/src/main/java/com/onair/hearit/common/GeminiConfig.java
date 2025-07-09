package com.onair.hearit.common;

import com.onair.hearit.infrastructure.GeminiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiConfig {

    private final String key;

    public GeminiConfig(@Value("${gemini-api-key}") String key) {
        this.key = key;
    }

    @Bean
    public GeminiClient geminiClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        return new GeminiClient(restClient, key);
    }
}
