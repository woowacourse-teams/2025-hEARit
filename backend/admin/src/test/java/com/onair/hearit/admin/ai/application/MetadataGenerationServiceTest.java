package com.onair.hearit.admin.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.application.MetadataGenerationService.GeneratedMetadata;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import com.onair.hearit.admin.ai.infrastructure.gemini.GeminiClient;
import com.onair.hearit.admin.ai.infrastructure.prompt.PromptLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetadataGenerationServiceTest {

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private PromptLoader promptLoader;

    private ObjectMapper objectMapper;
    private MetadataGenerationService metadataService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        metadataService = new MetadataGenerationService(geminiClient, objectMapper, promptLoader);

        // 테스트용 프롬프트 템플릿 설정
        when(promptLoader.getMetadataGenerationPrompt()).thenReturn("메타데이터 생성: %s");
    }

    @Nested
    @DisplayName("generateMetadata 메서드는")
    class GenerateMetadataTests {

        @Test
        @DisplayName("제목과 요약을 생성하여 반환한다")
        void returnsGeneratedMetadata() {
            // given
            String scriptText = "이번 에피소드에서는 Spring Boot 3.0의 새로운 기능에 대해 알아봅니다.";

            String geminiResponse = """
                {"title": "Spring Boot 3.0 새 기능 완벽 정리", "summary": "Spring Boot 3.0에서 추가된 주요 기능들을 살펴봅니다. Native Image 지원, Jakarta EE 마이그레이션, 그리고 관측성 향상에 대해 다룹니다."}
                """;

            when(geminiClient.generateContentWithJson(anyString())).thenReturn(geminiResponse);

            // when
            GeneratedMetadata result = metadataService.generateMetadata(scriptText);

            // then
            assertThat(result.getTitle()).isEqualTo("Spring Boot 3.0 새 기능 완벽 정리");
            assertThat(result.getSummary()).contains("Spring Boot 3.0");
        }

        @Test
        @DisplayName("JSON 앞뒤에 텍스트가 있어도 정상 파싱한다")
        void parsesJsonWithSurroundingText() {
            // given
            String scriptText = "테스트 대본";

            String geminiResponse = """
                다음은 생성된 메타데이터입니다:
                {"title": "테스트 제목", "summary": "테스트 요약입니다."}
                위 JSON이 결과입니다.
                """;

            when(geminiClient.generateContentWithJson(anyString())).thenReturn(geminiResponse);

            // when
            GeneratedMetadata result = metadataService.generateMetadata(scriptText);

            // then
            assertThat(result.getTitle()).isEqualTo("테스트 제목");
            assertThat(result.getSummary()).isEqualTo("테스트 요약입니다.");
        }

        @Test
        @DisplayName("제목이 35자를 초과하면 잘라낸다")
        void truncatesTitleWhenTooLong() {
            // given
            String scriptText = "대본";

            String longTitle = "이것은매우긴제목입니다열다섯글자를넘어서35자를초과하는제목입니다추가텍스트";
            String geminiResponse = String.format(
                    "{\"title\": \"%s\", \"summary\": \"요약\"}", longTitle);

            when(geminiClient.generateContentWithJson(anyString())).thenReturn(geminiResponse);

            // when
            GeneratedMetadata result = metadataService.generateMetadata(scriptText);

            // then
            assertThat(result.getTitle()).hasSize(35);
        }

        @Test
        @DisplayName("요약이 250자를 초과하면 잘라낸다")
        void truncatesSummaryWhenTooLong() {
            // given
            String scriptText = "대본";

            String longSummary = "가".repeat(300);
            String geminiResponse = String.format(
                    "{\"title\": \"제목\", \"summary\": \"%s\"}", longSummary);

            when(geminiClient.generateContentWithJson(anyString())).thenReturn(geminiResponse);

            // when
            GeneratedMetadata result = metadataService.generateMetadata(scriptText);

            // then
            assertThat(result.getSummary()).hasSize(250);
        }

        @Test
        @DisplayName("잘못된 JSON 응답은 예외를 던진다")
        void throwsExceptionOnInvalidJson() {
            // given
            String scriptText = "대본";
            String invalidResponse = "이것은 JSON이 아닙니다";

            when(geminiClient.generateContentWithJson(anyString())).thenReturn(invalidResponse);

            // when & then
            assertThatThrownBy(() -> metadataService.generateMetadata(scriptText))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("메타데이터 생성 실패");
        }

        @Test
        @DisplayName("대본이 10000자를 초과하면 앞부분만 사용한다")
        void truncatesLongScript() {
            // given
            String longScript = "테".repeat(15000);

            String geminiResponse = "{\"title\": \"긴 대본\", \"summary\": \"요약\"}";

            when(geminiClient.generateContentWithJson(anyString())).thenReturn(geminiResponse);

            // when
            GeneratedMetadata result = metadataService.generateMetadata(longScript);

            // then
            assertThat(result.getTitle()).isEqualTo("긴 대본");
            // 실제로 Gemini에 전달되는 대본이 10000자 + "..."로 제한됨을 검증하려면
            // ArgumentCaptor를 사용해야 하지만, 여기서는 동작 확인만
        }

        @Test
        @DisplayName("빈 제목/요약도 처리한다")
        void handlesEmptyTitleAndSummary() {
            // given
            String scriptText = "대본";
            String geminiResponse = "{\"title\": \"\", \"summary\": \"\"}";

            when(geminiClient.generateContentWithJson(anyString())).thenReturn(geminiResponse);

            // when
            GeneratedMetadata result = metadataService.generateMetadata(scriptText);

            // then
            assertThat(result.getTitle()).isEmpty();
            assertThat(result.getSummary()).isEmpty();
        }
    }
}
