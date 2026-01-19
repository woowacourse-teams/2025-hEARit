package com.onair.hearit.admin.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.application.MetadataGenerationService.GeneratedMetadata;
import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.exception.AudioProcessingException;
import com.onair.hearit.admin.ai.infrastructure.audio.AudioProcessor;
import com.onair.hearit.admin.ai.infrastructure.audio.AudioProcessorResolver;
import com.onair.hearit.admin.ai.infrastructure.groq.GroqWhisperClient.TranscriptionResult;
import com.onair.hearit.admin.ai.infrastructure.jpa.AiProcessResultRepository;
import com.onair.hearit.admin.ai.infrastructure.storage.TempFileManager;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiProcessingServiceTest {

    @Mock
    private AiProcessResultRepository resultRepository;

    @Mock
    private AiProcessStatusUpdater statusUpdater;

    @Mock
    private FileStorage fileStorage;

    @Mock
    private TempFileManager tempFileManager;

    @Mock
    private AudioProcessorResolver audioProcessorResolver;

    @Mock
    private AudioProcessor audioProcessor;

    @Mock
    private TranscriptionService transcriptionService;

    @Mock
    private ScriptCorrectionService correctionService;

    @Mock
    private MetadataGenerationService metadataService;

    private ObjectMapper objectMapper;
    private AiProcessingService aiProcessingService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        aiProcessingService = new AiProcessingService(
                resultRepository,
                statusUpdater,
                fileStorage,
                tempFileManager,
                audioProcessorResolver,
                transcriptionService,
                correctionService,
                metadataService,
                objectMapper,
                24,           // expirationHours
                60,           // shortsDurationSeconds
                "hearit/temp/" // tempPrefix
        );

        // 기본 AudioProcessor 동작 설정
        when(audioProcessorResolver.resolve(anyString())).thenReturn(audioProcessor);
        when(audioProcessor.getExtension()).thenReturn("mp3");
        when(audioProcessor.getMimeType()).thenReturn("audio/mpeg");
    }

    @Nested
    @DisplayName("startProcessing 메서드는")
    class StartProcessingTests {

        @Test
        @DisplayName("유효한 파일로 AiProcessResult를 생성하고 ID를 반환한다")
        void createsAiProcessResultAndReturnsId() {
            // given
            byte[] audioData = createTestAudioData();
            String filename = "test.mp3";

            AiProcessResult savedResult = createMockResult(1L);
            when(resultRepository.save(any(AiProcessResult.class))).thenReturn(savedResult);

            // when
            Long resultId = aiProcessingService.startProcessing(audioData, filename);

            // then
            assertThat(resultId).isEqualTo(1L);
            verify(audioProcessorResolver).resolve(filename);
            verify(audioProcessor).validate(audioData, filename);
            verify(resultRepository).save(any(AiProcessResult.class));
        }

        @Test
        @DisplayName("생성된 엔티티의 상태가 PENDING이다")
        void createdEntityHasPendingStatus() {
            // given
            byte[] audioData = createTestAudioData();
            String filename = "test.mp3";

            ArgumentCaptor<AiProcessResult> captor = ArgumentCaptor.forClass(AiProcessResult.class);
            when(resultRepository.save(captor.capture()))
                    .thenAnswer(inv -> {
                        AiProcessResult result = inv.getArgument(0);
                        ReflectionTestUtils.setField(result, "id", 1L);
                        return result;
                    });

            // when
            aiProcessingService.startProcessing(audioData, filename);

            // then
            AiProcessResult captured = captor.getValue();
            assertThat(captured.getStatus()).isEqualTo(ProcessStatus.PENDING);
            assertThat(captured.getOriginalFileName()).isEqualTo(filename);
            assertThat(captured.getOriginalFileKey()).startsWith("hearit/temp/original/");
        }

        @Test
        @DisplayName("유효하지 않은 파일은 예외를 던진다")
        void throwsExceptionForInvalidFile() {
            // given
            byte[] audioData = createTestAudioData();
            String filename = "test.wav";

            doThrow(AudioProcessingException.unsupportedFormat("지원하지 않는 오디오 형식입니다"))
                    .when(audioProcessor).validate(any(), anyString());

            // when & then
            assertThatThrownBy(() -> aiProcessingService.startProcessing(audioData, filename))
                    .isInstanceOf(AudioProcessingException.class)
                    .hasMessageContaining("지원하지 않는 오디오 형식");

            verify(resultRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("executeProcessing 메서드는")
    class ExecuteProcessingTests {

        @Test
        @DisplayName("전체 AI 처리 플로우를 순차적으로 실행한다")
        void executesFullProcessingFlow() {
            // given
            Long processId = 1L;
            byte[] audioData = createTestAudioData();

            AiProcessResult mockResult = createMockResult(processId);
            when(statusUpdater.findById(processId)).thenReturn(mockResult);

            byte[] shortsData = new byte[500];
            when(audioProcessor.createShortClip(any(byte[].class), anyInt())).thenReturn(shortsData);

            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "테스트")
            );
            TranscriptionResult transcription = new TranscriptionResult(60.0, rawSegments);
            when(transcriptionService.transcribe(any(byte[].class), anyString()))
                    .thenReturn(transcription);

            List<ScriptSegment> correctedSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "테스트 교정됨")
            );
            when(correctionService.correctScript(any())).thenReturn(correctedSegments);

            when(transcriptionService.mergeSegmentsToText(any())).thenReturn("테스트 교정됨");
            when(metadataService.generateMetadata(anyString()))
                    .thenReturn(new GeneratedMetadata("테스트 제목", "테스트 요약"));

            // when
            aiProcessingService.executeProcessing(processId, audioData);

            // then
            // 상태 변경 순서 검증
            verify(statusUpdater).markAsUploading(processId);
            verify(statusUpdater).markAsConverting(processId);
            verify(statusUpdater).markAsTranscribing(processId);
            verify(statusUpdater).markAsCorrecting(processId);
            verify(statusUpdater).markAsGeneratingMeta(processId);
            verify(statusUpdater).markAsCompleted(eq(processId), any());

            // S3 업로드 검증 (원본 1회 + org/shr 2회 + script json 1회)
            verify(fileStorage, times(3)).uploadBytes(any(byte[].class), anyString(), eq("audio/mpeg"));
            verify(fileStorage).uploadBytes(any(byte[].class), anyString(), eq("application/json"));

            // 완료 상태 검증
            verify(statusUpdater).setSuggestedMetadata(processId, "테스트 제목", "테스트 요약");
        }

        @Test
        @DisplayName("STT 처리 실패 시 FAILED 상태로 변경한다")
        void marksAsFailedWhenSttFails() {
            // given
            Long processId = 1L;
            byte[] audioData = createTestAudioData();

            AiProcessResult mockResult = createMockResult(processId);
            when(statusUpdater.findById(processId)).thenReturn(mockResult);

            byte[] shortsData = new byte[500];
            when(audioProcessor.createShortClip(any(byte[].class), anyInt())).thenReturn(shortsData);

            when(transcriptionService.transcribe(any(byte[].class), anyString()))
                    .thenThrow(new AudioProcessingException("STT 처리 실패"));

            // when
            aiProcessingService.executeProcessing(processId, audioData);

            // then
            verify(statusUpdater).markAsFailed(eq(processId), anyString());
        }

        @Test
        @DisplayName("메타데이터 생성 실패 시 FAILED 상태로 변경한다")
        void marksAsFailedWhenMetadataGenerationFails() {
            // given
            Long processId = 1L;
            byte[] audioData = createTestAudioData();

            AiProcessResult mockResult = createMockResult(processId);
            when(statusUpdater.findById(processId)).thenReturn(mockResult);

            byte[] shortsData = new byte[500];
            when(audioProcessor.createShortClip(any(byte[].class), anyInt())).thenReturn(shortsData);

            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "테스트")
            );
            TranscriptionResult transcription = new TranscriptionResult(60.0, rawSegments);
            when(transcriptionService.transcribe(any(byte[].class), anyString()))
                    .thenReturn(transcription);

            when(correctionService.correctScript(any())).thenReturn(rawSegments);
            when(transcriptionService.mergeSegmentsToText(any())).thenReturn("테스트");
            when(metadataService.generateMetadata(anyString()))
                    .thenThrow(new AudioProcessingException("메타데이터 생성 실패"));

            // when
            aiProcessingService.executeProcessing(processId, audioData);

            // then
            verify(statusUpdater).markAsFailed(eq(processId), anyString());
            verify(statusUpdater, never()).markAsCompleted(anyLong(), any());
        }

        @Test
        @DisplayName("원본이 60초 미만이면 쇼츠가 원본과 동일하다")
        void shortsEqualsOriginalWhenUnder60Seconds() {
            // given
            Long processId = 1L;
            byte[] audioData = createTestAudioData();

            AiProcessResult mockResult = createMockResult(processId);
            when(statusUpdater.findById(processId)).thenReturn(mockResult);

            // 원본이 짧아서 그대로 반환
            when(audioProcessor.createShortClip(any(byte[].class), anyInt())).thenReturn(audioData);

            List<ScriptSegment> rawSegments = List.of(
                    new ScriptSegment(0, 0, 5000, "짧은 오디오")
            );
            TranscriptionResult transcription = new TranscriptionResult(30.0, rawSegments);
            when(transcriptionService.transcribe(any(byte[].class), anyString()))
                    .thenReturn(transcription);

            when(correctionService.correctScript(any())).thenReturn(rawSegments);
            when(transcriptionService.mergeSegmentsToText(any())).thenReturn("짧은 오디오");
            when(metadataService.generateMetadata(anyString()))
                    .thenReturn(new GeneratedMetadata("제목", "요약"));

            // when
            aiProcessingService.executeProcessing(processId, audioData);

            // then
            verify(audioProcessor).createShortClip(audioData, 60);
            verify(statusUpdater).markAsCompleted(eq(processId), any());
        }
    }

    private byte[] createTestAudioData() {
        byte[] data = new byte[1000];
        data[0] = 'I';
        data[1] = 'D';
        data[2] = '3';
        return data;
    }

    private AiProcessResult createMockResult(Long id) {
        AiProcessResult result = AiProcessResult.builder()
                .originalFileName("test.mp3")
                .originalFileKey("hearit/temp/original/test-uuid.mp3")
                .build();
        ReflectionTestUtils.setField(result, "id", id);
        return result;
    }
}
