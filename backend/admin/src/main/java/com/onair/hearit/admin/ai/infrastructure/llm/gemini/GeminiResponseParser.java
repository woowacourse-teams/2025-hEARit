package com.onair.hearit.admin.ai.infrastructure.llm.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.exception.LlmResponseParseException;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiResponseParser {

    private static final String PROVIDER_NAME = "gemini";

    private final ObjectMapper objectMapper;

    public LlmResponse parse(String jsonResponse, long latencyMs) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new LlmResponseParseException("Gemini 응답이 비어있습니다", jsonResponse);
        }

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                log.error("Gemini 응답에 candidates가 없음");
                throw new LlmResponseParseException("Gemini 응답 형식 오류: candidates 없음", jsonResponse);
            }

            JsonNode firstCandidate = candidates.get(0);
            String finishReason = firstCandidate.path("finishReason").asText("UNKNOWN");

            if (!"STOP".equals(finishReason)) {
                log.warn("Gemini 응답 종료 이유: {} (정상은 STOP)", finishReason);
            }

            JsonNode parts = firstCandidate.path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                log.error("Gemini 응답에 parts가 없음");
                throw new LlmResponseParseException("Gemini 응답 형식 오류: parts 없음", jsonResponse);
            }

            String text = parts.get(0).path("text").asText("");

            log.debug("Gemini 응답 텍스트 추출 완료: 길이={}, finishReason={}", text.length(), finishReason);

            return LlmResponse.builder()
                    .text(text)
                    .finishReason(finishReason)
                    .latencyMs(latencyMs)
                    .provider(PROVIDER_NAME)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Gemini 응답 파싱 실패", e);
            throw new LlmResponseParseException("Gemini 응답 파싱 실패", jsonResponse, e);
        }
    }
}
