package com.onair.hearit.admin.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import com.onair.hearit.admin.ai.infrastructure.gemini.GeminiClient;
import com.onair.hearit.admin.ai.infrastructure.prompt.PromptLoader;
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
    private final PromptLoader promptLoader;

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
            String prompt = String.format(promptLoader.getScriptCorrectionPrompt(), inputJson);

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
