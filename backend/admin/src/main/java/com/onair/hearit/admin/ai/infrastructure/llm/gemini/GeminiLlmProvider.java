package com.onair.hearit.admin.ai.infrastructure.llm.gemini;

import com.onair.hearit.admin.ai.config.LlmProviderProperties.GeminiProperties;
import com.onair.hearit.admin.ai.exception.LlmApiException;
import com.onair.hearit.admin.ai.exception.LlmRateLimitException;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmProvider;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmRequest;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmResponse;
import com.onair.hearit.admin.ai.infrastructure.llm.ratelimit.TokenBucketRateLimiter;
import com.onair.hearit.admin.ai.infrastructure.llm.retry.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@RequiredArgsConstructor
public class GeminiLlmProvider implements LlmProvider {

    private static final String PROVIDER_NAME = "gemini";
    private static final String URL_TEMPLATE = "%s/models/%s:generateContent?key=%s";

    private final RestClient restClient;
    private final GeminiProperties properties;
    private final GeminiRequestBuilder requestBuilder;
    private final GeminiResponseParser responseParser;
    private final TokenBucketRateLimiter rateLimiter;
    private final RetryPolicy retryPolicy;

    @Override
    public LlmResponse generate(LlmRequest request) {
        return executeWithRetry(request, false);
    }

    @Override
    public LlmResponse generateJson(LlmRequest request) {
        return executeWithRetry(request, true);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    private LlmResponse executeWithRetry(LlmRequest request, boolean jsonResponse) {
        log.debug("Gemini API 호출: 프롬프트 길이={}, JSON응답={}", request.getPrompt().length(), jsonResponse);

        return retryPolicy.execute(() -> executeRequest(request, jsonResponse));
    }

    private LlmResponse executeRequest(LlmRequest request, boolean jsonResponse) {
        try {
            rateLimiter.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmApiException("요청 대기 중 인터럽트 발생", e);
        }

        String url = buildUrl();
        String requestBody = requestBuilder.buildRequestBody(request, jsonResponse);

        try {
            long startTime = System.currentTimeMillis();

            String body = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode().value() == 429) {
                            throw new LlmRateLimitException("Gemini API 호출 횟수 초과");
                        }
                        throw new LlmApiException("Gemini API 호출 실패: " + res.getStatusCode());
                    })
                    .body(String.class);

            long latencyMs = System.currentTimeMillis() - startTime;

            log.info("Gemini API 응답 수신: {}ms 소요", latencyMs);

            if (body == null) {
                throw new LlmApiException("Gemini API 응답 본문이 비어있습니다");
            }

            return responseParser.parse(body, latencyMs);

        } catch (LlmApiException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.error("Gemini API 호출 실패: {}", e.getStatusCode(), e);
            throw new LlmApiException("Gemini API 호출 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Gemini API 호출 실패", e);
            throw new LlmApiException("Gemini API 호출 실패: " + e.getMessage(), e);
        }
    }

    private String buildUrl() {
        return String.format(URL_TEMPLATE,
                properties.getBaseUrl(),
                properties.getModel(),
                properties.getApiKey());
    }
}
