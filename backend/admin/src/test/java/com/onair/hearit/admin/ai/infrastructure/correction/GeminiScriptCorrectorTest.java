package com.onair.hearit.admin.ai.infrastructure.correction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.infrastructure.gemini.GeminiApiClient;
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
class GeminiScriptCorrectorTest {

    @Mock
    private GeminiApiClient geminiApiClient;

    @Mock
    private PromptLoader promptLoader;

    private ObjectMapper objectMapper;
    private GeminiScriptCorrector scriptCorrector;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        scriptCorrector = new GeminiScriptCorrector(geminiApiClient, objectMapper, promptLoader);
        when(promptLoader.getScriptCorrectionPrompt()).thenReturn("교정 요청: %s");
    }

    @Nested
    @DisplayName("correct 메서드는")
    class CorrectTests {

        @Test
        @DisplayName("교정된 세그먼트 목록을 반환한다")
        void returnsCorrectedSegments() {
            // given
            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "자바 스크립트로 개발합니다"),
                    new ScriptSegment(1, 5000, 10000, "리액트 사용해요")
            );

            String geminiResponse = """
                [
                    {"id": 0, "start": 0, "end": 5000, "text": "JavaScript로 개발합니다"},
                    {"id": 1, "start": 5000, "end": 10000, "text": "React 사용해요"}
                ]
                """;

            when(geminiApiClient.generateContentWithJson(anyString())).thenReturn(geminiResponse);

            // when
            List<ScriptSegment> result = scriptCorrector.correct(rawSegments);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getText()).isEqualTo("JavaScript로 개발합니다");
            assertThat(result.get(1).getText()).isEqualTo("React 사용해요");
            // id, start, end는 그대로 유지되어야 함
            assertThat(result.get(0).getId()).isZero();
            assertThat(result.get(0).getStart()).isZero();
            assertThat(result.get(0).getEnd()).isEqualTo(5000);
        }

        @Test
        @DisplayName("JSON 배열 앞뒤에 텍스트가 있어도 정상 파싱한다")
        void parsesJsonWithSurroundingText() {
            // given
            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "테스트")
            );

            // Gemini가 추가 텍스트를 붙이는 경우
            String geminiResponse = """
                교정된 결과입니다:
                [{"id": 0, "start": 0, "end": 5000, "text": "테스트 교정됨"}]
                위 내용이 교정 결과입니다.
                """;

            when(geminiApiClient.generateContentWithJson(anyString())).thenReturn(geminiResponse);

            // when
            List<ScriptSegment> result = scriptCorrector.correct(rawSegments);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getText()).isEqualTo("테스트 교정됨");
        }

        @Test
        @DisplayName("세그먼트 수가 다르면 원본을 반환한다")
        void returnsFallbackWhenSegmentCountDiffers() {
            // given
            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "원본 텍스트1"),
                    new ScriptSegment(1, 5000, 10000, "원본 텍스트2")
            );

            // Gemini가 세그먼트 수를 잘못 반환한 경우
            String geminiResponse = """
                [{"id": 0, "start": 0, "end": 10000, "text": "병합된 텍스트"}]
                """;

            when(geminiApiClient.generateContentWithJson(anyString())).thenReturn(geminiResponse);

            // when
            List<ScriptSegment> result = scriptCorrector.correct(rawSegments);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getText()).isEqualTo("원본 텍스트1");
            assertThat(result.get(1).getText()).isEqualTo("원본 텍스트2");
        }

        @Test
        @DisplayName("잘못된 JSON 응답이면 원본을 반환한다")
        void returnsFallbackOnInvalidJson() {
            // given
            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "원본")
            );

            String invalidResponse = "이것은 JSON이 아닙니다";

            when(geminiApiClient.generateContentWithJson(anyString())).thenReturn(invalidResponse);

            // when
            List<ScriptSegment> result = scriptCorrector.correct(rawSegments);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getText()).isEqualTo("원본");
        }

        @Test
        @DisplayName("빈 세그먼트 목록도 처리한다")
        void handlesEmptySegments() {
            // given
            List<ScriptSegment> rawSegments = List.of();

            when(geminiApiClient.generateContentWithJson(anyString())).thenReturn("[]");

            // when
            List<ScriptSegment> result = scriptCorrector.correct(rawSegments);

            // then
            assertThat(result).isEmpty();
        }
    }
}
