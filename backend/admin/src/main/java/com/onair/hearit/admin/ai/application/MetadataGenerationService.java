package com.onair.hearit.admin.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import com.onair.hearit.admin.ai.infrastructure.gemini.GeminiClient;
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

    private static final String METADATA_PROMPT_TEMPLATE = """
            당신은 IT/기술 콘텐츠 큐레이터입니다.

            아래 대본을 읽고 제목과 요약을 생성해주세요.

            규칙:
            1. 제목 (title):
               - 35자 이내
               - 콘텐츠의 핵심 주제를 명확하게 전달
               - 호기심을 유발하는 매력적인 제목
               - 이모지 사용 금지

            2. 요약 (summary):
               - 250자 이내
               - 콘텐츠의 주요 내용을 3-4문장으로 요약
               - 사용자가 들을지 결정할 수 있도록 핵심 정보 포함
               - 이모지 사용 금지

            대본:
            %s

            출력 형식 (JSON):
            {"title": "제목", "summary": "요약"}
            """;

    /**
     * 대본에서 제목과 요약 생성
     *
     * @param scriptText 전체 대본 텍스트
     * @return 생성된 메타데이터 (제목, 요약)
     */
    public GeneratedMetadata generateMetadata(String scriptText) {
        log.info("메타데이터 생성 시작: 대본 길이={}", scriptText.length());

        // 대본이 너무 길면 앞부분만 사용 (토큰 제한)
        String truncatedScript = truncateScript(scriptText, 10000);

        String prompt = String.format(METADATA_PROMPT_TEMPLATE, truncatedScript);

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
            // JSON 객체 추출
            String cleanJson = extractJsonObject(responseJson);

            JsonNode root = objectMapper.readTree(cleanJson);

            String title = root.path("title").asText("").trim();
            String summary = root.path("summary").asText("").trim();

            // 길이 제한 적용
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

    /**
     * 응답에서 JSON 객체 부분만 추출
     */
    private String extractJsonObject(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');

        if (start == -1 || end == -1 || start >= end) {
            return response;
        }

        return response.substring(start, end + 1);
    }

    /**
     * 대본 길이 제한
     */
    private String truncateScript(String script, int maxLength) {
        if (script.length() <= maxLength) {
            return script;
        }
        return script.substring(0, maxLength) + "...";
    }

    /**
     * 생성된 메타데이터 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class GeneratedMetadata {
        private final String title;
        private final String summary;
    }
}
