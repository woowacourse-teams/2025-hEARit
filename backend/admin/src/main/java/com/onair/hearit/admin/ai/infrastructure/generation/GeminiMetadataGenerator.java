package com.onair.hearit.admin.ai.infrastructure.generation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import com.onair.hearit.admin.ai.infrastructure.gemini.GeminiApiClient;
import com.onair.hearit.admin.ai.infrastructure.prompt.PromptLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiMetadataGenerator implements MetadataGenerator {

    private static final int MAX_SCRIPT_LENGTH = 10000;
    private static final int MAX_TITLE_LENGTH = 35;
    private static final int MAX_SUMMARY_LENGTH = 250;

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;

    @Override
    public GeneratedMetadata generate(String scriptText) {
        log.info("메타데이터 생성 시작: 대본 길이={}", scriptText.length());

        String truncatedScript = truncateScript(scriptText, MAX_SCRIPT_LENGTH);
        String prompt = String.format(promptLoader.getMetadataGenerationPrompt(), truncatedScript);
        String responseJson = geminiApiClient.generateContentWithJson(prompt);

        GeneratedMetadata metadata = parseResponse(responseJson);

        log.info("메타데이터 생성 완료: 제목='{}', 요약 길이={}",
                metadata.getTitle(), metadata.getSummary().length());

        return metadata;
    }

    private GeneratedMetadata parseResponse(String responseJson) {
        try {
            String cleanJson = extractJsonObject(responseJson);
            JsonNode root = objectMapper.readTree(cleanJson);
            String title = root.path("title").asText("").trim();
            String summary = root.path("summary").asText("").trim();
            if (title.length() > MAX_TITLE_LENGTH) {
                title = title.substring(0, MAX_TITLE_LENGTH);
            }
            if (summary.length() > MAX_SUMMARY_LENGTH) {
                summary = summary.substring(0, MAX_SUMMARY_LENGTH);
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
}
