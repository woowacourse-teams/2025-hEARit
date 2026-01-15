package com.onair.hearit.admin.ai.infrastructure.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class GeminiClient {

    private static final String GEMINI_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    public GeminiClient(
            @Qualifier("aiRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 텍스트 생성 (일반)
     *
     * @param prompt 프롬프트
     * @return 생성된 텍스트
     */
    public String generateContent(String prompt) {
        log.debug("Gemini API 호출: 프롬프트 길이={}", prompt.length());

        String requestBody = buildRequestBody(prompt, false);
        String response = callGeminiApi(requestBody);

        return extractTextFromResponse(response);
    }

    /**
     * JSON 응답 형식으로 텍스트 생성
     *
     * @param prompt 프롬프트 (JSON 형식 요청 포함)
     * @return JSON 응답 텍스트
     */
    public String generateContentWithJson(String prompt) {
        log.debug("Gemini API 호출 (JSON): 프롬프트 길이={}", prompt.length());

        String requestBody = buildRequestBody(prompt, true);
        String response = callGeminiApi(requestBody);

        return extractTextFromResponse(response);
    }

    /**
     * 요청 바디 생성
     */
    private String buildRequestBody(String prompt, boolean jsonResponse) {
        try {
            ObjectNode root = objectMapper.createObjectNode();

            // contents
            ArrayNode contents = root.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", prompt);

            // generationConfig
            ObjectNode config = root.putObject("generationConfig");
            config.put("temperature", 0.7);
            config.put("maxOutputTokens", 8192);

            if (jsonResponse) {
                config.put("responseMimeType", "application/json");
            }

            return objectMapper.writeValueAsString(root);

        } catch (JsonProcessingException e) {
            throw new AudioProcessingException("Gemini 요청 생성 실패", e);
        }
    }

    /**
     * Gemini API 호출
     */
    private String callGeminiApi(String requestBody) {
        String url = String.format(GEMINI_URL_TEMPLATE, model, apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, requestEntity, String.class);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Gemini API 응답 수신: {}ms 소요", elapsed);

            return response.getBody();

        } catch (RestClientException e) {
            log.error("Gemini API 호출 실패", e);
            throw AudioProcessingException.apiCallFailed("LLM 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Gemini 응답에서 텍스트 추출
     *
     * 응답 형식:
     * {
     *   "candidates": [
     *     {
     *       "content": {
     *         "parts": [
     *           { "text": "응답 텍스트" }
     *         ]
     *       }
     *     }
     *   ]
     * }
     */
    private String extractTextFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                log.error("Gemini 응답에 candidates가 없음: {}", jsonResponse);
                throw new AudioProcessingException("Gemini 응답 형식 오류");
            }

            JsonNode firstCandidate = candidates.get(0);
            JsonNode parts = firstCandidate.path("content").path("parts");

            if (!parts.isArray() || parts.isEmpty()) {
                log.error("Gemini 응답에 parts가 없음: {}", jsonResponse);
                throw new AudioProcessingException("Gemini 응답 형식 오류");
            }

            String text = parts.get(0).path("text").asText("");

            log.debug("Gemini 응답 텍스트 추출 완료: 길이={}", text.length());

            return text;

        } catch (JsonProcessingException e) {
            log.error("Gemini 응답 파싱 실패: {}", jsonResponse, e);
            throw new AudioProcessingException("Gemini 응답 파싱 실패", e);
        }
    }
}
