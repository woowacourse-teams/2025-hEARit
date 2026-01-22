package com.onair.hearit.admin.ai.infrastructure.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.config.LlmProviderProperties.MetadataProperties;
import com.onair.hearit.admin.ai.exception.LlmResponseParseException;
import com.onair.hearit.admin.ai.infrastructure.generation.MetadataGenerator.GeneratedMetadata;
import com.onair.hearit.admin.ai.infrastructure.json.JsonExtractor;
import com.onair.hearit.admin.ai.infrastructure.json.TextTruncator;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmProvider;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmRequest;
import com.onair.hearit.admin.ai.infrastructure.llm.LlmResponse;
import com.onair.hearit.admin.ai.infrastructure.prompt.PromptLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetadataGeneratorTest {

    @Mock
    private LlmProvider llmProvider;

    @Mock
    private PromptLoader promptLoader;

    private ObjectMapper objectMapper;
    private JsonExtractor jsonExtractor;
    private TextTruncator textTruncator;
    private MetadataProperties metadataProperties;
    private MetadataGenerator metadataGenerator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jsonExtractor = new JsonExtractor();
        textTruncator = new TextTruncator();
        metadataProperties = new MetadataProperties();
        metadataProperties.setMaxScriptLength(10000);
        metadataProperties.setMaxTitleLength(35);
        metadataProperties.setMaxSummaryLength(250);

        metadataGenerator = new MetadataGenerator(
                llmProvider, objectMapper, promptLoader, jsonExtractor, textTruncator, metadataProperties);
    }

    private void stubPromptLoader() {
        when(promptLoader.getMetadataGenerationPrompt()).thenReturn("메타데이터 생성: %s");
    }

    @Nested
    @DisplayName("generate 메서드는")
    class GenerateTests {

        @Test
        @DisplayName("제목과 요약을 생성하여 반환한다")
        void returnsGeneratedMetadata() {
            // given
            stubPromptLoader();
            String scriptText = "이번 에피소드에서는 Spring Boot 3.0의 새로운 기능에 대해 알아봅니다.";

            String llmResponse = """
                {"title": "Spring Boot 3.0 새 기능 완벽 정리", "summary": "Spring Boot 3.0에서 추가된 주요 기능들을 살펴봅니다."}
                """;

            when(llmProvider.generateJson(any(LlmRequest.class)))
                    .thenReturn(LlmResponse.builder()
                            .text(llmResponse)
                            .finishReason("STOP")
                            .latencyMs(100)
                            .provider("gemini")
                            .build());

            // when
            GeneratedMetadata result = metadataGenerator.generate(scriptText);

            // then
            assertThat(result.getTitle()).isEqualTo("Spring Boot 3.0 새 기능 완벽 정리");
            assertThat(result.getSummary()).contains("Spring Boot 3.0");
        }

        @Test
        @DisplayName("JSON 앞뒤에 텍스트가 있어도 정상 파싱한다")
        void parsesJsonWithSurroundingText() {
            // given
            stubPromptLoader();
            String scriptText = "테스트 대본";

            String llmResponse = """
                다음은 생성된 메타데이터입니다:
                {"title": "테스트 제목", "summary": "테스트 요약입니다."}
                위 JSON이 결과입니다.
                """;

            when(llmProvider.generateJson(any(LlmRequest.class)))
                    .thenReturn(LlmResponse.builder()
                            .text(llmResponse)
                            .finishReason("STOP")
                            .latencyMs(100)
                            .provider("gemini")
                            .build());

            // when
            GeneratedMetadata result = metadataGenerator.generate(scriptText);

            // then
            assertThat(result.getTitle()).isEqualTo("테스트 제목");
            assertThat(result.getSummary()).isEqualTo("테스트 요약입니다.");
        }

        @Test
        @DisplayName("제목이 35자를 초과하면 잘라낸다")
        void truncatesTitleWhenTooLong() {
            // given
            stubPromptLoader();
            String scriptText = "대본";

            String longTitle = "이것은매우긴제목입니다열다섯글자를넘어서35자를초과하는제목입니다추가텍스트";
            String llmResponse = String.format(
                    "{\"title\": \"%s\", \"summary\": \"요약\"}", longTitle);

            when(llmProvider.generateJson(any(LlmRequest.class)))
                    .thenReturn(LlmResponse.builder()
                            .text(llmResponse)
                            .finishReason("STOP")
                            .latencyMs(100)
                            .provider("gemini")
                            .build());

            // when
            GeneratedMetadata result = metadataGenerator.generate(scriptText);

            // then
            assertThat(result.getTitle().length()).isLessThanOrEqualTo(35);
        }

        @Test
        @DisplayName("요약이 250자를 초과하면 잘라낸다")
        void truncatesSummaryWhenTooLong() {
            // given
            stubPromptLoader();
            String scriptText = "대본";

            String longSummary = "가".repeat(300);
            String llmResponse = String.format(
                    "{\"title\": \"제목\", \"summary\": \"%s\"}", longSummary);

            when(llmProvider.generateJson(any(LlmRequest.class)))
                    .thenReturn(LlmResponse.builder()
                            .text(llmResponse)
                            .finishReason("STOP")
                            .latencyMs(100)
                            .provider("gemini")
                            .build());

            // when
            GeneratedMetadata result = metadataGenerator.generate(scriptText);

            // then
            assertThat(result.getSummary().length()).isLessThanOrEqualTo(250);
        }

        @Test
        @DisplayName("잘못된 JSON 응답은 예외를 던진다")
        void throwsExceptionOnInvalidJson() {
            // given
            stubPromptLoader();
            String scriptText = "대본";
            String invalidResponse = "이것은 JSON이 아닙니다";

            when(llmProvider.generateJson(any(LlmRequest.class)))
                    .thenReturn(LlmResponse.builder()
                            .text(invalidResponse)
                            .finishReason("STOP")
                            .latencyMs(100)
                            .provider("gemini")
                            .build());

            // when & then
            assertThatThrownBy(() -> metadataGenerator.generate(scriptText))
                    .isInstanceOf(LlmResponseParseException.class);
        }

        @Test
        @DisplayName("빈 제목/요약도 처리한다")
        void handlesEmptyTitleAndSummary() {
            // given
            stubPromptLoader();
            String scriptText = "대본";
            String llmResponse = "{\"title\": \"\", \"summary\": \"\"}";

            when(llmProvider.generateJson(any(LlmRequest.class)))
                    .thenReturn(LlmResponse.builder()
                            .text(llmResponse)
                            .finishReason("STOP")
                            .latencyMs(100)
                            .provider("gemini")
                            .build());

            // when
            GeneratedMetadata result = metadataGenerator.generate(scriptText);

            // then
            assertThat(result.getTitle()).isEmpty();
            assertThat(result.getSummary()).isEmpty();
        }

        @Test
        @DisplayName("null 입력은 예외를 던진다")
        void throwsExceptionOnNullInput() {
            // when & then
            assertThatThrownBy(() -> metadataGenerator.generate(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 문자열 입력은 예외를 던진다")
        void throwsExceptionOnEmptyInput() {
            // when & then
            assertThatThrownBy(() -> metadataGenerator.generate(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
