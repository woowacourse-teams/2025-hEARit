package com.onair.hearit.admin.ai.application;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.infrastructure.jpa.AiProcessResultRepository;
import com.onair.hearit.admin.application.AdminHearitService;
import com.onair.hearit.admin.dto.request.HearitMetaDataRequest;
import com.onair.hearit.admin.dto.request.HearitMetaDataRequest.SourceCreateRequest;
import com.onair.hearit.admin.exception.custom.AdminNotFoundException;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.core.domain.FileType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 처리 결과 관리 서비스
 * 결과 조회, 수정, 확인 및 Hearit 등록
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiResultService {

    private final AiProcessResultRepository resultRepository;
    private final AdminHearitService hearitService;
    private final FileStorage fileStorage;

    @Value("${aws.s3.bucket.url}")
    private String bucketUrl;

    /**
     * 처리 결과 조회
     */
    @Transactional(readOnly = true)
    public AiProcessResult getResult(Long processId) {
        return resultRepository.findById(processId)
                .orElseThrow(() -> new AdminNotFoundException("AI 처리 결과", processId.toString()));
    }

    /**
     * 처리 상태 조회
     */
    @Transactional(readOnly = true)
    public AiProcessResult getStatus(Long processId) {
        return getResult(processId);
    }

    /**
     * 대본 수정
     */
    @Transactional
    public void updateScript(Long processId, List<ScriptSegment> editedScript) {
        AiProcessResult result = getResult(processId);
        validateEditable(result);

        result.updateEditedScript(editedScript);
        resultRepository.save(result);

        log.info("대본 수정 완료: processId={}, 세그먼트={}개", processId, editedScript.size());
    }

    /**
     * 메타데이터(제목, 요약) 수정
     */
    @Transactional
    public void updateMetadata(Long processId, String title, String summary) {
        AiProcessResult result = getResult(processId);
        validateEditable(result);

        result.updateEditedMetadata(title, summary);
        resultRepository.save(result);

        log.info("메타데이터 수정 완료: processId={}, 제목='{}'", processId, title);
    }

    /**
     * 검토 완료 및 Hearit 등록
     */
    @Transactional
    public Long confirmAndCreateHearit(
            Long processId,
            Long categoryId,
            List<Long> keywordIds,
            List<SourceCreateRequest> sources,
            String finalTitle,
            String finalSummary,
            List<ScriptSegment> finalScript) {

        AiProcessResult result = getResult(processId);
        validateConfirmable(result);

        log.info("Hearit 등록 시작: processId={}", processId);

        // 1. 최종값 저장
        if (finalTitle != null) {
            result.updateEditedMetadata(finalTitle, finalSummary);
        }
        if (finalScript != null) {
            result.updateEditedScript(finalScript);
        }

        // 2. S3 파일 이동 (temp → 정식 경로)
        String newUuid = UUID.randomUUID().toString();
        String orgKey = moveToFinalPath(result.getGeneratedOrgKey(), FileType.ORIGINAL, newUuid);
        String shrKey = moveToFinalPath(result.getGeneratedShrKey(), FileType.SHORT, newUuid);
        String scrKey = moveToFinalPath(result.getGeneratedScrKey(), FileType.SCRIPT, newUuid);

        // 3. Hearit 생성
        HearitMetaDataRequest request = new HearitMetaDataRequest(
                result.getFinalTitle(),
                result.getFinalSummary(),
                result.getPlayTime(),
                categoryId,
                keywordIds,
                sources,
                orgKey,
                shrKey,
                scrKey
        );

        hearitService.addHearitMetaData(request);

        // 4. 상태 업데이트 (confirmedHearit는 따로 조회해서 설정해야 하지만 일단 상태만 변경)
        result.markAsConfirmed(null);
        resultRepository.save(result);

        // 5. 원본 temp 파일 삭제
        cleanupOriginalTempFile(result);

        log.info("Hearit 등록 완료: processId={}", processId);

        return processId;
    }

    /**
     * AI 결과 삭제 (취소)
     */
    @Transactional
    public void deleteResult(Long processId) {
        AiProcessResult result = getResult(processId);

        // S3 임시 파일 삭제
        deleteIfExists(result.getOriginalFileKey());
        deleteIfExists(result.getGeneratedOrgKey());
        deleteIfExists(result.getGeneratedShrKey());
        deleteIfExists(result.getGeneratedScrKey());

        // DB 레코드 삭제
        resultRepository.delete(result);

        log.info("AI 결과 삭제 완료: processId={}", processId);
    }

    /**
     * S3 파일을 temp에서 정식 경로로 이동
     */
    private String moveToFinalPath(String tempKey, FileType fileType, String uuid) {
        if (tempKey == null || tempKey.isBlank()) {
            return null;
        }

        String extension = getExtension(tempKey);
        String prefix = fileType.name().substring(0, 3).toUpperCase();
        String newKey = fileType.getUploadPath() + prefix + "_" + uuid + extension;

        return fileStorage.moveFile(tempKey, newKey);
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    private void cleanupOriginalTempFile(AiProcessResult result) {
        deleteIfExists(result.getOriginalFileKey());
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

    private void validateEditable(AiProcessResult result) {
        if (result.getStatus() != ProcessStatus.COMPLETED) {
            throw new IllegalStateException("완료된 처리 결과만 수정할 수 있습니다. 현재 상태: " + result.getStatus());
        }
    }

    private void validateConfirmable(AiProcessResult result) {
        if (result.getStatus() != ProcessStatus.COMPLETED) {
            throw new IllegalStateException("완료된 처리 결과만 확인할 수 있습니다. 현재 상태: " + result.getStatus());
        }
    }

    /**
     * 오디오 파일 URL 생성
     */
    public String getAudioUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return bucketUrl + "/" + key;
    }
}
