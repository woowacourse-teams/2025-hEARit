package com.onair.hearit.admin.ai.infrastructure.correction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.exception.LlmResponseParseException;
import com.onair.hearit.admin.ai.infrastructure.json.JsonExtractor;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmProvider;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmRequest;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmResponse;
import com.onair.hearit.admin.ai.infrastructure.prompt.PromptLoader;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptCorrectorTest {

    @Mock
    private LlmProvider llmProvider;

    @Mock
    private PromptLoader promptLoader;

    private ObjectMapper objectMapper;
    private JsonExtractor jsonExtractor;
    private ScriptCorrector scriptCorrector;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jsonExtractor = new JsonExtractor();
        scriptCorrector = new ScriptCorrector(llmProvider, objectMapper, promptLoader, jsonExtractor);
    }

    private void stubPromptLoader() {
        when(promptLoader.getScriptCorrectionPrompt()).thenReturn("교정 요청: %s");
    }

    @Nested
    @DisplayName("correct 메서드는")
    class CorrectTests {

        @Test
        @DisplayName("교정된 세그먼트 목록을 반환한다")
        void returnsCorrectedSegments() {
            // given
            stubPromptLoader();
            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "자바 스크립트로 개발합니다"),
                    new ScriptSegment(1, 5000, 10000, "리액트 사용해요")
            );

            String llmResponse = """
                [
                    {"id": 0, "start": 0, "end": 5000, "text": "JavaScript로 개발합니다"},
                    {"id": 1, "start": 5000, "end": 10000, "text": "React 사용해요"}
                ]
                """;

            when(llmProvider.generateJson(any(LlmRequest.class)))
                    .thenReturn(LlmResponse.builder()
                            .text(llmResponse)
                            .finishReason("STOP")
                            .latencyMs(100)
                            .provider("gemini")
                            .build());

            // when
            List<ScriptSegment> result = scriptCorrector.correct(rawSegments);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getText()).isEqualTo("JavaScript로 개발합니다");
            assertThat(result.get(1).getText()).isEqualTo("React 사용해요");
            assertThat(result.get(0).getId()).isZero();
            assertThat(result.get(0).getStart()).isZero();
            assertThat(result.get(0).getEnd()).isEqualTo(5000);
        }

        @Test
        @DisplayName("JSON 배열 앞뒤에 텍스트가 있어도 정상 파싱한다")
        void parsesJsonWithSurroundingText() {
            // given
            stubPromptLoader();
            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "테스트")
            );

            String llmResponse = """
                교정된 결과입니다:
                [{"id": 0, "start": 0, "end": 5000, "text": "테스트 교정됨"}]
                위 내용이 교정 결과입니다.
                """;

            when(llmProvider.generateJson(any(LlmRequest.class)))
                    .thenReturn(LlmResponse.builder()
                            .text(llmResponse)
                            .finishReason("STOP")
                            .latencyMs(100)
                            .provider("gemini")
                            .build());

            // when
            List<ScriptSegment> result = scriptCorrector.correct(rawSegments);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getText()).isEqualTo("테스트 교정됨");
        }

        @Test
        @DisplayName("세그먼트 수가 다르면 예외를 던진다")
        void throwsExceptionWhenSegmentCountDiffers() {
            // given
            stubPromptLoader();
            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "원본 텍스트1"),
                    new ScriptSegment(1, 5000, 10000, "원본 텍스트2")
            );

            String llmResponse = """
                [{"id": 0, "start": 0, "end": 10000, "text": "병합된 텍스트"}]
                """;

            when(llmProvider.generateJson(any(LlmRequest.class)))
                    .thenReturn(LlmResponse.builder()
                            .text(llmResponse)
                            .finishReason("STOP")
                            .latencyMs(100)
                            .provider("gemini")
                            .build());

            // when & then
            assertThatThrownBy(() -> scriptCorrector.correct(rawSegments))
                    .isInstanceOf(LlmResponseParseException.class)
                    .hasMessageContaining("세그먼트 수 불일치");
        }

        @Test
        @DisplayName("잘못된 JSON 응답이면 예외를 던진다")
        void throwsExceptionOnInvalidJson() {
            // given
            stubPromptLoader();
            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "원본")
            );

            String invalidResponse = "이것은 JSON이 아닙니다";

            when(llmProvider.generateJson(any(LlmRequest.class)))
                    .thenReturn(LlmResponse.builder()
                            .text(invalidResponse)
                            .finishReason("STOP")
                            .latencyMs(100)
                            .provider("gemini")
                            .build());

            // when & then
            assertThatThrownBy(() -> scriptCorrector.correct(rawSegments))
                    .isInstanceOf(LlmResponseParseException.class);
        }

        @Test
        @DisplayName("빈 세그먼트 목록은 그대로 반환한다")
        void handlesEmptySegments() {
            // given
            List<ScriptSegment> rawSegments = List.of();

            // when
            List<ScriptSegment> result = scriptCorrector.correct(rawSegments);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null 입력은 예외를 던진다")
        void throwsExceptionOnNullInput() {
            // when & then
            assertThatThrownBy(() -> scriptCorrector.correct(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
