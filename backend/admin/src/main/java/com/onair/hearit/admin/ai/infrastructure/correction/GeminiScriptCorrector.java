package com.onair.hearit.admin.ai.infrastructure.correction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import com.onair.hearit.admin.ai.infrastructure.gemini.GeminiApiClient;
import com.onair.hearit.admin.ai.infrastructure.prompt.PromptLoader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiScriptCorrector implements ScriptCorrector {

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;

    @Override
    public List<ScriptSegment> correct(List<ScriptSegment> rawSegments) {
        log.info("대본 교정 시작: 세그먼트 {}개", rawSegments.size());
        try {
            String inputJson = objectMapper.writeValueAsString(rawSegments);
            String prompt = String.format(promptLoader.getScriptCorrectionPrompt(), inputJson);
            String responseJson = geminiApiClient.generateContentWithJson(prompt);
            List<ScriptSegment> correctedSegments = parseResponse(responseJson, rawSegments);
            log.info("대본 교정 완료: 세그먼트 {}개", correctedSegments.size());
            return correctedSegments;
        } catch (JsonProcessingException e) {
            log.error("대본 교정 중 JSON 처리 실패", e);
            throw new AudioProcessingException("대본 교정 실패", e);
        }
    }

    private List<ScriptSegment> parseResponse(String responseJson, List<ScriptSegment> fallback) {
        try {
            String cleanJson = extractJsonArray(responseJson);
            List<ScriptSegment> result = objectMapper.readValue(cleanJson, new TypeReference<>() {});
            if (result.size() != fallback.size()) {
                log.warn("교정 결과 세그먼트 수 불일치: 원본={}, 교정={}. 원본 유지.",
                        fallback.size(), result.size());
                return fallback;
            }
            return result;
        } catch (JsonProcessingException e) {
            log.warn("교정 응답 파싱 실패, 원본 유지: {}", e.getMessage());
            int len = responseJson.length();
            String tail = len > 500 ? responseJson.substring(len - 500) : responseJson;
            log.warn("응답 끝부분 (마지막 500자):\n{}", tail);
            return fallback;
        }
    }

    private String extractJsonArray(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');

        if (start == -1 || end == -1 || start >= end) {
            return response;
        }

        return response.substring(start, end + 1);
    }
}
