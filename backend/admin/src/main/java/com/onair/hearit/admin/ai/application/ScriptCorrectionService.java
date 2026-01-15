package com.onair.hearit.admin.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import com.onair.hearit.admin.ai.infrastructure.gemini.GeminiClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 대본 교정 서비스
 * Gemini API를 사용하여 STT 결과의 오타/전문용어를 교정
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScriptCorrectionService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    private static final String CORRECTION_PROMPT_TEMPLATE = """
            당신은 IT/기술 전문 콘텐츠의 대본 교정 전문가입니다.

            아래는 음성 인식(STT)으로 생성된 대본입니다. 다음 규칙에 따라 교정해주세요:

            1. IT 전문용어 교정:
               - 잘못 인식된 기술 용어를 올바르게 수정 (예: "자바 스크립트" → "JavaScript")
               - 영어 기술 용어는 원어 그대로 유지 (예: "리액트" → "React", "스프링" → "Spring")

            2. 오타 및 문법 교정:
               - 명백한 오타 수정
               - 어색한 문장 자연스럽게 다듬기
               - 불필요한 추임새 제거 ("음", "어", "그" 등)

            3. 유지해야 할 것:
               - 각 세그먼트의 id, start, end 값은 절대 변경하지 마세요
               - 원래 의미와 말투는 유지하세요
               - 세그먼트 순서를 변경하지 마세요

            입력 JSON:
            %s

            출력 형식: 동일한 구조의 JSON 배열로 반환해주세요.
            [{"id": 0, "start": 0, "end": 1000, "text": "교정된 텍스트"}, ...]
            """;

    /**
     * STT 결과 대본을 교정
     *
     * @param rawSegments STT로 생성된 원본 세그먼트 목록
     * @return 교정된 세그먼트 목록
     */
    public List<ScriptSegment> correctScript(List<ScriptSegment> rawSegments) {
        log.info("대본 교정 시작: 세그먼트 {}개", rawSegments.size());

        try {
            // 입력 JSON 생성
            String inputJson = objectMapper.writeValueAsString(rawSegments);

            // 프롬프트 생성
            String prompt = String.format(CORRECTION_PROMPT_TEMPLATE, inputJson);

            // Gemini API 호출
            String responseJson = geminiClient.generateContentWithJson(prompt);

            // 응답 파싱
            List<ScriptSegment> correctedSegments = parseResponse(responseJson, rawSegments);

            log.info("대본 교정 완료: 세그먼트 {}개", correctedSegments.size());

            return correctedSegments;

        } catch (JsonProcessingException e) {
            log.error("대본 교정 중 JSON 처리 실패", e);
            throw new AudioProcessingException("대본 교정 실패", e);
        }
    }

    /**
     * Gemini 응답을 파싱하여 ScriptSegment 목록으로 변환
     */
    private List<ScriptSegment> parseResponse(String responseJson, List<ScriptSegment> fallback) {
        try {
            // JSON 배열 시작/끝 찾기 (Gemini가 추가 텍스트를 붙일 수 있음)
            String cleanJson = extractJsonArray(responseJson);

            List<ScriptSegment> result = objectMapper.readValue(
                    cleanJson, new TypeReference<>() {});

            // 결과 검증: 세그먼트 수가 다르면 원본 반환
            if (result.size() != fallback.size()) {
                log.warn("교정 결과 세그먼트 수 불일치: 원본={}, 교정={}. 원본 유지.",
                        fallback.size(), result.size());
                return fallback;
            }

            return result;

        } catch (JsonProcessingException e) {
            log.warn("교정 응답 파싱 실패, 원본 유지: {}", e.getMessage());
            return fallback;
        }
    }

    /**
     * 응답에서 JSON 배열 부분만 추출
     */
    private String extractJsonArray(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');

        if (start == -1 || end == -1 || start >= end) {
            return response;
        }

        return response.substring(start, end + 1);
    }
}
