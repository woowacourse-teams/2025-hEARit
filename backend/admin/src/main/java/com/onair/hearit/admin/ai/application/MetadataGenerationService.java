package com.onair.hearit.admin.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import com.onair.hearit.admin.ai.infrastructure.gemini.GeminiClient;
import com.onair.hearit.admin.ai.infrastructure.prompt.PromptLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 메타데이터 생성 서비스
 * Gemini API를 사용하여 대본에서 제목과 요약을 생성
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MetadataGenerationService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;

    public GeneratedMetadata generateMetadata(String scriptText) {
        log.info("메타데이터 생성 시작: 대본 길이={}", scriptText.length());
        String truncatedScript = truncateScript(scriptText, 10000);
        String prompt = String.format(promptLoader.getMetadataGenerationPrompt(), truncatedScript);
        String responseJson = geminiClient.generateContentWithJson(prompt);
        GeneratedMetadata metadata = parseResponse(responseJson);
        log.info("메타데이터 생성 완료: 제목='{}', 요약 길이={}",
                metadata.getTitle(), metadata.getSummary().length());
        return metadata;
    }

    /**
     * 응답 파싱
     */
    private GeneratedMetadata parseResponse(String responseJson) {
        try {
            String cleanJson = extractJsonObject(responseJson);
            JsonNode root = objectMapper.readTree(cleanJson);
            String title = root.path("title").asText("").trim();
            String summary = root.path("summary").asText("").trim();
            if (title.length() > 35) {
                title = title.substring(0, 35);
            }
            if (summary.length() > 250) {
                summary = summary.substring(0, 250);
            }
            return new GeneratedMetadata(title, summary);
        } catch (JsonProcessingException e) {
            log.error("메타데이터 응답 파싱 실패: {}", responseJson, e);
            throw new AudioProcessingException("메타데이터 생성 실패", e);
        }
    }

    private String extractJsonObject(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');

        if (start == -1 || end == -1 || start >= end) {
            return response;
        }
        return response.substring(start, end + 1);
    }

    private String truncateScript(String script, int maxLength) {
        if (script.length() <= maxLength) {
            return script;
        }
        return script.substring(0, maxLength) + "...";
    }

    @Getter
    @AllArgsConstructor
    public static class GeneratedMetadata {
        private final String title;
        private final String summary;
    }
}
