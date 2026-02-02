package com.onair.hearit.admin.ai.infrastructure.stt.groq;

import com.onair.hearit.admin.ai.config.SttProviderProperties.GroqProperties;
import com.onair.hearit.admin.ai.exception.LlmApiException;
import com.onair.hearit.admin.ai.exception.LlmRateLimitException;
import com.onair.hearit.admin.ai.infrastructure.stt.SttProvider;
import com.onair.hearit.admin.ai.infrastructure.stt.SttRequest;
import com.onair.hearit.admin.ai.infrastructure.stt.SttResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@RequiredArgsConstructor
public class GroqSttProvider implements SttProvider {

    private static final String PROVIDER_NAME = "groq";

    private final RestClient restClient;
    private final GroqProperties properties;
    private final GroqRequestBuilder requestBuilder;
    private final GroqResponseParser responseParser;

    @Override
    @Retryable(
            retryFor = {LlmRateLimitException.class, ResourceAccessException.class},
            maxAttempts = 4,
            backoff = @Backoff(delay = 2000, multiplier = 2.0, maxDelay = 30000)
    )
    public SttResponse transcribe(SttRequest request) {
        log.info("Groq Whisper API 호출 시작: 파일={}, 크기={}KB, 모델={}",
                request.getFilename(), request.getAudioData().length / 1024, properties.getModel());

        return executeRequest(request);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public int getMaxFileSizeBytes() {
        return properties.getMaxFileSizeBytes();
    }

    @Override
    public boolean isAvailable() {
        return properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    private SttResponse executeRequest(SttRequest request) {
        MultiValueMap<String, Object> multipartBody = requestBuilder.buildMultipartBody(request);

        try {
            long startTime = System.currentTimeMillis();

            String body = restClient.post()
                    .uri(properties.getBaseUrl())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .header("Authorization", "Bearer " + requestBuilder.getApiKey())
                    .body(multipartBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode().value() == 429) {
                            throw new LlmRateLimitException("Groq Whisper API 호출 횟수 초과");
                        }
                        throw new LlmApiException("Groq Whisper API 호출 실패: " + res.getStatusCode());
                    })
                    .body(String.class);

            long latencyMs = System.currentTimeMillis() - startTime;

            log.info("Groq Whisper API 응답 수신: {}ms 소요", latencyMs);

            if (body == null) {
                throw new LlmApiException("Groq Whisper API 응답 본문이 비어있습니다");
            }

            return responseParser.parse(body, latencyMs);

        } catch (LlmApiException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.error("Groq Whisper API 호출 실패: {}", e.getStatusCode(), e);
            throw new LlmApiException("Groq Whisper API 호출 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Groq Whisper API 호출 실패", e);
            throw new LlmApiException("Groq Whisper API 호출 실패: " + e.getMessage(), e);
        }
    }
}
