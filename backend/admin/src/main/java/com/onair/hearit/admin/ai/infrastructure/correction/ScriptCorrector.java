package com.onair.hearit.admin.ai.infrastructure.correction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import com.onair.hearit.admin.ai.exception.LlmResponseParseException;
import com.onair.hearit.admin.ai.infrastructure.json.JsonExtractor;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmProvider;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmRequest;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmResponse;
import com.onair.hearit.admin.ai.infrastructure.prompt.PromptLoader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScriptCorrector {

    private final LlmProvider llmProvider;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;
    private final JsonExtractor jsonExtractor;

    public List<ScriptSegment> correct(List<ScriptSegment> rawSegments) {
        if (rawSegments == null) {
            throw new IllegalArgumentException("rawSegments는 null일 수 없습니다");
        }

        if (rawSegments.isEmpty()) {
            log.info("교정할 세그먼트가 없습니다");
            return rawSegments;
        }

        log.info("대본 교정 시작: 세그먼트 {}개", rawSegments.size());

        try {
            String inputJson = objectMapper.writeValueAsString(rawSegments);
            String prompt = String.format(promptLoader.getScriptCorrectionPrompt(), inputJson);

            LlmResponse response = llmProvider.generateJson(LlmRequest.of(prompt));

            if (!response.isComplete()) {
                log.warn("LLM 응답이 완전하지 않음: finishReason={}", response.getFinishReason());
            }

            List<ScriptSegment> correctedSegments = parseResponse(response.getText(), rawSegments.size());

            log.info("대본 교정 완료: 세그먼트 {}개", correctedSegments.size());
            return correctedSegments;

        } catch (JsonProcessingException e) {
            log.error("대본 교정 중 JSON 처리 실패", e);
            throw new AudioProcessingException("대본 교정 실패: JSON 직렬화 오류", e);
        }
    }

    private List<ScriptSegment> parseResponse(String responseJson, int expectedSize) {
        String cleanJson = jsonExtractor.extractJsonArray(responseJson);

        try {
            List<ScriptSegment> result = objectMapper.readValue(cleanJson, new TypeReference<>() {});

            if (result.size() != expectedSize) {
                log.error("교정 결과 세그먼트 수 불일치: 원본={}, 교정={}", expectedSize, result.size());
                throw new LlmResponseParseException(
                        String.format("세그먼트 수 불일치: 원본=%d, 교정=%d", expectedSize, result.size()),
                        responseJson
                );
            }

            return result;

        } catch (JsonProcessingException e) {
            log.error("교정 응답 파싱 실패", e);
            throw new LlmResponseParseException("교정 응답 파싱 실패", responseJson, e);
        }
    }
}
