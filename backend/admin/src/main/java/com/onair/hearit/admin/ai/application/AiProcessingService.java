package com.onair.hearit.admin.ai.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.admin.ai.application.MetadataGenerationService.GeneratedMetadata;
import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.infrastructure.audio.Mp3AudioProcessor;
import com.onair.hearit.admin.ai.infrastructure.jpa.AiProcessResultRepository;
import com.onair.hearit.admin.ai.infrastructure.groq.GroqWhisperClient.TranscriptionResult;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 처리 오케스트레이션 서비스
 * 전체 AI 처리 플로우를 관리하고 각 단계를 순차적으로 실행
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiProcessingService {

    private final AiProcessResultRepository resultRepository;
    private final AiProcessStatusUpdater statusUpdater;
    private final FileStorage fileStorage;
    private final Mp3AudioProcessor mp3Processor;
    private final TranscriptionService transcriptionService;
    private final ScriptCorrectionService correctionService;
    private final MetadataGenerationService metadataService;
    private final ObjectMapper objectMapper;

    @Value("${ai.result.expiration.hours:24}")
    private int expirationHours;

    @Value("${ai.shorts.duration.seconds:60}")
    private int shortsDurationSeconds;

    @Value("${aws.s3.temp.prefix:hearit/temp/}")
    private String tempPrefix;

    /**
     * AI 처리 시작 (동기 - 엔티티 생성만)
     *
     * @param audioData 오디오 바이트 배열
     * @param filename 원본 파일명
     * @return 생성된 처리 결과 ID
     */
    @Transactional
    public Long startProcessing(byte[] audioData, String filename) {
        log.info("AI 처리 시작: 파일={}, 크기={}KB", filename, audioData.length / 1024);

        // 파일 검증
        mp3Processor.validateMp3(audioData, filename);

        // 결과 엔티티 생성
        String uuid = UUID.randomUUID().toString();
        String originalKey = tempPrefix + "original/" + uuid + ".mp3";

        AiProcessResult result = AiProcessResult.builder()
                .originalFileName(filename)
                .originalFileKey(originalKey)
                .build();

        result = resultRepository.save(result);

        log.info("AI 처리 엔티티 생성 완료: id={}", result.getId());

        return result.getId();
    }

    /**
     * AI 처리 실행 (비동기)
     *
     * 각 단계의 상태 업데이트는 AiProcessStatusUpdater를 통해 별도 트랜잭션으로 처리됩니다.
     * 이를 통해 폴링 클라이언트가 실시간으로 진행 상태를 확인할 수 있습니다.
     */
    @Async("aiProcessingExecutor")
    public void executeProcessing(Long processId, byte[] audioData) {
        log.info("AI 처리 비동기 실행 시작: processId={}", processId);

        AiProcessResult result = statusUpdater.findById(processId);
        String uuid = extractUuid(result.getOriginalFileKey());
        String originalFileKey = result.getOriginalFileKey();
        String originalFileName = result.getOriginalFileName();

        try {
            // 1. 원본 파일 S3 업로드
            uploadOriginalFile(processId, originalFileKey, audioData);

            // 2. 쇼츠 생성 및 업로드
            createAndUploadShorts(processId, audioData, uuid);

            // 3. STT 처리
            TranscriptionResult transcription = processTranscription(processId, audioData, originalFileName);

            // 4. 대본 교정
            List<ScriptSegment> correctedScript = correctScript(processId, transcription.getSegments());

            // 5. 대본 JSON 파일 생성 및 업로드
            uploadScriptFile(processId, correctedScript, uuid);

            // 6. 메타데이터 생성
            generateMetadata(processId, correctedScript);

            // 7. 완료 처리
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);
            statusUpdater.markAsCompleted(processId, expiresAt);

            log.info("AI 처리 완료: processId={}", processId);

        } catch (Exception e) {
            log.error("AI 처리 실패: processId={}", processId, e);
            statusUpdater.markAsFailed(processId, e.getMessage());

            // 실패 시 생성된 임시 파일 정리
            cleanupTempFiles(processId);
        }
    }

    /**
     * 1. 원본 파일 업로드
     */
    private void uploadOriginalFile(Long processId, String originalFileKey, byte[] audioData) {
        statusUpdater.markAsUploading(processId);

        log.debug("원본 파일 업로드 중: {}", originalFileKey);
        fileStorage.uploadBytes(audioData, originalFileKey, "audio/mpeg");
    }

    /**
     * 2. 쇼츠 생성 및 업로드
     */
    private void createAndUploadShorts(Long processId, byte[] audioData, String uuid) {
        statusUpdater.markAsConverting(processId);

        log.debug("쇼츠 생성 중: {}초", shortsDurationSeconds);

        // 쇼츠 생성
        byte[] shortsData = mp3Processor.createShortClip(audioData, shortsDurationSeconds);

        // S3 업로드
        String orgKey = tempPrefix + "org/" + uuid + ".mp3";
        String shrKey = tempPrefix + "shr/" + uuid + ".mp3";

        fileStorage.uploadBytes(audioData, orgKey, "audio/mpeg");
        fileStorage.uploadBytes(shortsData, shrKey, "audio/mpeg");

        statusUpdater.setGeneratedFiles(processId, orgKey, shrKey, null);
    }

    /**
     * 3. STT 처리
     */
    private TranscriptionResult processTranscription(Long processId, byte[] audioData, String originalFileName) {
        statusUpdater.markAsTranscribing(processId);

        log.debug("STT 처리 중...");

        TranscriptionResult transcription = transcriptionService.transcribe(audioData, originalFileName);

        statusUpdater.setTranscriptionResult(
                processId,
                transcription.getSegments(),
                transcription.getDurationSeconds()
        );

        return transcription;
    }

    /**
     * 4. 대본 교정
     */
    private List<ScriptSegment> correctScript(Long processId, List<ScriptSegment> rawSegments) {
        statusUpdater.markAsCorrecting(processId);

        log.debug("대본 교정 중...");

        List<ScriptSegment> correctedScript = correctionService.correctScript(rawSegments);
        statusUpdater.setCorrectedScript(processId, correctedScript);

        return correctedScript;
    }

    /**
     * 5. 대본 JSON 파일 업로드
     */
    private void uploadScriptFile(Long processId, List<ScriptSegment> script, String uuid) {
        try {
            byte[] scriptJson = objectMapper.writeValueAsBytes(script);
            String scrKey = tempPrefix + "scr/" + uuid + ".json";

            fileStorage.uploadBytes(scriptJson, scrKey, "application/json");

            // generatedScrKey 설정 - 기존 orgKey, shrKey 유지하면서 scrKey만 추가
            AiProcessResult current = statusUpdater.findById(processId);
            statusUpdater.setGeneratedFiles(
                    processId,
                    current.getGeneratedOrgKey(),
                    current.getGeneratedShrKey(),
                    scrKey
            );

        } catch (Exception e) {
            log.warn("대본 파일 업로드 실패 (처리 계속)", e);
        }
    }

    /**
     * 6. 메타데이터 생성
     */
    private void generateMetadata(Long processId, List<ScriptSegment> script) {
        statusUpdater.markAsGeneratingMeta(processId);

        log.debug("메타데이터 생성 중...");

        String fullText = transcriptionService.mergeSegmentsToText(script);
        GeneratedMetadata metadata = metadataService.generateMetadata(fullText);

        statusUpdater.setSuggestedMetadata(processId, metadata.getTitle(), metadata.getSummary());
    }

    /**
     * 실패 시 임시 파일 정리
     */
    private void cleanupTempFiles(Long processId) {
        try {
            AiProcessResult result = statusUpdater.findById(processId);
            deleteIfExists(result.getOriginalFileKey());
            deleteIfExists(result.getGeneratedOrgKey());
            deleteIfExists(result.getGeneratedShrKey());
            deleteIfExists(result.getGeneratedScrKey());
        } catch (Exception e) {
            log.warn("임시 파일 정리 중 오류 (무시)", e);
        }
    }

    private void deleteIfExists(String key) {
        if (key != null && !key.isBlank()) {
            try {
                fileStorage.deleteFile(key);
            } catch (Exception e) {
                log.debug("파일 삭제 실패 (무시): {}", key);
            }
        }
    }

    /**
     * S3 키에서 UUID 추출
     */
    private String extractUuid(String key) {
        // hearit/temp/original/uuid.mp3 → uuid
        String filename = key.substring(key.lastIndexOf('/') + 1);
        return filename.replace(".mp3", "");
    }
}
