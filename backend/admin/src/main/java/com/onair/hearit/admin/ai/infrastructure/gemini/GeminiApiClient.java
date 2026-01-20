package com.onair.hearit.admin.ai.infrastructure.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class GeminiApiClient {

    private static final String GEMINI_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final Semaphore rateLimiter = new Semaphore(1);
    private static final long MIN_REQUEST_INTERVAL_MS = 4000;
    private static volatile long lastRequestTime = 0;
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 5000;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiApiClient(
            @Qualifier("aiRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generateContent(String prompt) {
        log.debug("Gemini API 호출: 프롬프트 길이={}", prompt.length());

        String requestBody = buildRequestBody(prompt, false);
        String response = callGeminiApi(requestBody);

        return extractTextFromResponse(response);
    }

    public String generateContentWithJson(String prompt) {
        log.debug("Gemini API 호출 (JSON): 프롬프트 길이={}", prompt.length());

        String requestBody = buildRequestBody(prompt, true);
        String response = callGeminiApi(requestBody);

        return extractTextFromResponse(response);
    }

    private String buildRequestBody(String prompt, boolean jsonResponse) {
        try {
            ObjectNode root = objectMapper.createObjectNode();

            ArrayNode contents = root.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", prompt);
            ObjectNode config = root.putObject("generationConfig");
            config.put("temperature", 0.7);
            config.put("maxOutputTokens", 65536);

            if (jsonResponse) {
                config.put("responseMimeType", "application/json");
            }

            return objectMapper.writeValueAsString(root);

        } catch (JsonProcessingException e) {
            throw new AudioProcessingException("Gemini 요청 생성 실패", e);
        }
    }

    private String callGeminiApi(String requestBody) {
        String url = String.format(GEMINI_URL_TEMPLATE, model, apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        int retryCount = 0;
        long retryDelay = INITIAL_RETRY_DELAY_MS;

        while (true) {
            try {
                rateLimiter.acquire();
                try {
                    waitForRateLimit();
                    long startTime = System.currentTimeMillis();
                    ResponseEntity<String> response = restTemplate.postForEntity(
                            url, requestEntity, String.class);
                    lastRequestTime = System.currentTimeMillis();
                    long elapsed = System.currentTimeMillis() - startTime;
                    log.info("Gemini API 응답 수신: {}ms 소요", elapsed);
                    return response.getBody();

                } finally {
                    rateLimiter.release();
                }

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && retryCount < MAX_RETRIES) {
                    retryCount++;
                    log.warn("Gemini API 429 에러, {}ms 후 재시도 ({}/{})",
                            retryDelay, retryCount, MAX_RETRIES);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw AudioProcessingException.apiCallFailed("재시도 중 인터럽트 발생", ie);
                    }
                    retryDelay *= 2;
                    continue;
                }

                log.error("Gemini API 호출 실패: {}", e.getStatusCode(), e);
                throw AudioProcessingException.apiCallFailed("LLM 처리 실패: " + e.getMessage(), e);

            } catch (RestClientException e) {
                log.error("Gemini API 호출 실패", e);
                throw AudioProcessingException.apiCallFailed("LLM 처리 실패: " + e.getMessage(), e);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw AudioProcessingException.apiCallFailed("요청 대기 중 인터럽트 발생", e);
            }
        }
    }

    private void waitForRateLimit() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;

        if (elapsed < MIN_REQUEST_INTERVAL_MS && lastRequestTime > 0) {
            long waitTime = MIN_REQUEST_INTERVAL_MS - elapsed;
            log.debug("Rate limit 대기: {}ms", waitTime);

            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                log.error("Gemini 응답에 candidates가 없음: {}", jsonResponse);
                throw new AudioProcessingException("Gemini 응답 형식 오류");
            }

            JsonNode firstCandidate = candidates.get(0);
            String finishReason = firstCandidate.path("finishReason").asText("UNKNOWN");
            if (!"STOP".equals(finishReason)) {
                log.warn("Gemini 응답 종료 이유: {} (정상은 STOP)", finishReason);
            }
            JsonNode parts = firstCandidate.path("content").path("parts");

            if (!parts.isArray() || parts.isEmpty()) {
                log.error("Gemini 응답에 parts가 없음: {}", jsonResponse);
                throw new AudioProcessingException("Gemini 응답 형식 오류");
            }

            String text = parts.get(0).path("text").asText("");

            log.debug("Gemini 응답 텍스트 추출 완료: 길이={}, finishReason={}", text.length(), finishReason);

            return text;

        } catch (JsonProcessingException e) {
            log.error("Gemini 응답 파싱 실패: {}", jsonResponse, e);
            throw new AudioProcessingException("Gemini 응답 파싱 실패", e);
        }
    }
}
