package com.onair.hearit.admin.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.infrastructure.generation.DefaultMetadataGenerator;
import com.onair.hearit.admin.ai.infrastructure.json.JsonExtractor;
import com.onair.hearit.admin.ai.infrastructure.json.TextTruncator;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmProvider;
import com.onair.hearit.admin.ai.infrastructure.llm.gemini.GeminiLlmProvider;
import com.onair.hearit.admin.ai.infrastructure.llm.gemini.GeminiRequestBuilder;
import com.onair.hearit.admin.ai.infrastructure.llm.gemini.GeminiResponseParser;
import com.onair.hearit.admin.ai.infrastructure.prompt.PromptLoader;
import com.onair.hearit.admin.ai.infrastructure.stt.SttProvider;
import com.onair.hearit.admin.ai.infrastructure.stt.groq.GroqRequestBuilder;
import com.onair.hearit.admin.ai.infrastructure.stt.groq.GroqResponseParser;
import com.onair.hearit.admin.ai.infrastructure.stt.groq.GroqSttProvider;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

@Configuration
@EnableAsync
@EnableRetry
@EnableConfigurationProperties({LlmProviderProperties.class, SttProviderProperties.class})
public class AiConfig {

    @Bean(name = "aiProcessingExecutor")
    public Executor aiProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("ai-process-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(name = "aiRestClient")
    public RestClient aiRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMinutes(5));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public GeminiRequestBuilder geminiRequestBuilder(
            ObjectMapper objectMapper,
            LlmProviderProperties properties) {
        return new GeminiRequestBuilder(objectMapper, properties.getGemini());
    }

    @Bean
    public LlmProvider geminiLlmProvider(
            @Qualifier("aiRestClient") RestClient restClient,
            LlmProviderProperties properties,
            GeminiRequestBuilder requestBuilder,
            GeminiResponseParser responseParser) {
        return new GeminiLlmProvider(
                restClient,
                properties.getGemini(),
                requestBuilder,
                responseParser
        );
    }

    @Bean
    public DefaultMetadataGenerator metadataGenerator(
            LlmProvider llmProvider,
            ObjectMapper objectMapper,
            PromptLoader promptLoader,
            JsonExtractor jsonExtractor,
            TextTruncator textTruncator,
            LlmProviderProperties properties) {
        return new DefaultMetadataGenerator(
                llmProvider,
                objectMapper,
                promptLoader,
                jsonExtractor,
                textTruncator,
                properties.getMetadata()
        );
    }

    @Bean
    public GroqRequestBuilder groqRequestBuilder(SttProviderProperties properties) {
        return new GroqRequestBuilder(properties.getGroq());
    }

    @Bean
    public SttProvider groqSttProvider(
            @Qualifier("aiRestClient") RestClient restClient,
            SttProviderProperties properties,
            GroqRequestBuilder requestBuilder,
            GroqResponseParser responseParser) {
        return new GroqSttProvider(
                restClient,
                properties.getGroq(),
                requestBuilder,
                responseParser
        );
    }
}
