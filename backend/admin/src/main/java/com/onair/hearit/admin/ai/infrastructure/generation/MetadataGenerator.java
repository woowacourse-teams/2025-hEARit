package com.onair.hearit.admin.ai.infrastructure.generation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.config.LlmProviderProperties.MetadataProperties;
import com.onair.hearit.admin.ai.exception.LlmResponseParseException;
import com.onair.hearit.admin.ai.infrastructure.json.JsonExtractor;
import com.onair.hearit.admin.ai.infrastructure.json.TextTruncator;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmProvider;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmRequest;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmResponse;
import com.onair.hearit.admin.ai.infrastructure.prompt.PromptLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MetadataGenerator {

    private final LlmProvider llmProvider;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;
    private final JsonExtractor jsonExtractor;
    private final TextTruncator textTruncator;
    private final MetadataProperties metadataProperties;

    public GeneratedMetadata generate(String scriptText) {
        if (scriptText == null || scriptText.isBlank()) {
            throw new IllegalArgumentException("scriptText는 null이거나 비어있을 수 없습니다");
        }

        log.info("메타데이터 생성 시작: 대본 길이={}", scriptText.length());

        String truncatedScript = textTruncator.truncateWithEllipsis(
                scriptText,
                metadataProperties.getMaxScriptLength()
        );
        String prompt = String.format(promptLoader.getMetadataGenerationPrompt(), truncatedScript);

        LlmResponse response = llmProvider.generateJson(LlmRequest.of(prompt));

        if (!response.isComplete()) {
            log.warn("LLM 응답이 완전하지 않음: finishReason={}", response.getFinishReason());
        }

        GeneratedMetadata metadata = parseResponse(response.getText());

        log.info("메타데이터 생성 완료: 제목='{}', 요약 길이={}",
                metadata.getTitle(), metadata.getSummary().length());

        return metadata;
    }

    private GeneratedMetadata parseResponse(String responseJson) {
        String cleanJson = jsonExtractor.extractJsonObject(responseJson);

        try {
            JsonNode root = objectMapper.readTree(cleanJson);

            String title = root.path("title").asText("").trim();
            String summary = root.path("summary").asText("").trim();

            title = textTruncator.truncate(title, metadataProperties.getMaxTitleLength());
            summary = textTruncator.truncate(summary, metadataProperties.getMaxSummaryLength());

            return new GeneratedMetadata(title, summary);

        } catch (JsonProcessingException e) {
            log.error("메타데이터 응답 파싱 실패", e);
            throw new LlmResponseParseException("메타데이터 응답 파싱 실패", responseJson, e);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class GeneratedMetadata {
        private final String title;
        private final String summary;
    }
}
