package com.onair.hearit.admin.ai.application;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.infrastructure.jpa.AiProcessResultRepository;
import com.onair.hearit.admin.ai.infrastructure.storage.TempFileManager;
import com.onair.hearit.admin.application.AdminHearitService;
import com.onair.hearit.admin.dto.request.HearitMetaDataRequest;
import com.onair.hearit.admin.dto.request.HearitMetaDataRequest.SourceCreateRequest;
import com.onair.hearit.admin.exception.custom.AdminNotFoundException;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.core.domain.FileType;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AiResultService {

    private final AiProcessResultRepository resultRepository;
    private final AdminHearitService hearitService;
    private final FileStorage fileStorage;
    private final TempFileManager tempFileManager;
    private final String bucketUrl;

    public AiResultService(
            AiProcessResultRepository resultRepository,
            AdminHearitService hearitService,
            FileStorage fileStorage,
            TempFileManager tempFileManager,
            @Value("${aws.s3.bucket.url}") String bucketUrl) {
        this.resultRepository = resultRepository;
        this.hearitService = hearitService;
        this.fileStorage = fileStorage;
        this.tempFileManager = tempFileManager;
        this.bucketUrl = bucketUrl;
    }

    @Transactional(readOnly = true)
    public AiProcessResult getResult(Long processId) {
        return resultRepository.findById(processId)
                .orElseThrow(() -> new AdminNotFoundException("AI 처리 결과", processId.toString()));
    }

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

    @Transactional
    public void updateMetadata(Long processId, String title, String summary) {
        AiProcessResult result = getResult(processId);
        validateEditable(result);

        result.updateEditedMetadata(title, summary);
        resultRepository.save(result);

        log.info("메타데이터 수정 완료: processId={}, 제목='{}'", processId, title);
    }

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
        if (finalTitle != null) {
            result.updateEditedMetadata(finalTitle, finalSummary);
        }
        if (finalScript != null) {
            result.updateEditedScript(finalScript);
        }
        String newUuid = UUID.randomUUID().toString();
        String orgKey = copyToFinalPath(result.getGeneratedOrgKey(), FileType.ORIGINAL, newUuid);
        String shrKey = copyToFinalPath(result.getGeneratedShrKey(), FileType.SHORT, newUuid);
        String scrKey = copyToFinalPath(result.getGeneratedScrKey(), FileType.SCRIPT, newUuid);
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
        result.markAsConfirmed(null);
        resultRepository.save(result);
        tempFileManager.cleanupTempFiles(result);
        log.info("Hearit 등록 완료: processId={}", processId);
        return processId;
    }

    @Transactional
    public void deleteResult(Long processId) {
        AiProcessResult result = getResult(processId);
        tempFileManager.cleanupTempFiles(result);
        resultRepository.delete(result);
        log.info("AI 결과 삭제 완료: processId={}", processId);
    }

    private String copyToFinalPath(String tempKey, FileType fileType, String uuid) {
        if (tempKey == null || tempKey.isBlank()) {
            return null;
        }
        String newKey = fileType.getUploadPath() + fileType.getPrefix() + "_" + uuid + fileType.getExtension();
        String copiedKey = fileStorage.copyFile(tempKey, newKey);
        return copiedKey.startsWith("/") ? copiedKey : "/" + copiedKey;
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

    public String getAudioUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return bucketUrl + "/" + key;
    }
}
