package com.onair.hearit.admin.ai.application;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.dto.ScriptSegment;
import com.onair.hearit.admin.ai.infrastructure.jpa.AiProcessResultRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiProcessStatusUpdater {

    private final AiProcessResultRepository resultRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiProcessResult findById(Long processId) {
        return resultRepository.findById(processId)
                .orElseThrow(() -> new IllegalArgumentException("처리 결과를 찾을 수 없습니다: " + processId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsUploading(Long processId) {
        AiProcessResult result = findResultById(processId);
        result.markAsUploading();
        resultRepository.save(result);
        log.debug("상태 변경: processId={}, status=UPLOADING", processId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsConverting(Long processId) {
        AiProcessResult result = findResultById(processId);
        result.markAsConverting();
        resultRepository.save(result);
        log.debug("상태 변경: processId={}, status=CONVERTING", processId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsTranscribing(Long processId) {
        AiProcessResult result = findResultById(processId);
        result.markAsTranscribing();
        resultRepository.save(result);
        log.debug("상태 변경: processId={}, status=TRANSCRIBING", processId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsCorrecting(Long processId) {
        AiProcessResult result = findResultById(processId);
        result.markAsCorrecting();
        resultRepository.save(result);
        log.debug("상태 변경: processId={}, status=CORRECTING", processId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsGeneratingMeta(Long processId) {
        AiProcessResult result = findResultById(processId);
        result.markAsGeneratingMeta();
        resultRepository.save(result);
        log.debug("상태 변경: processId={}, status=GENERATING_META", processId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsCompleted(Long processId, LocalDateTime expiresAt) {
        AiProcessResult result = findResultById(processId);
        result.markAsCompleted(expiresAt);
        resultRepository.save(result);
        log.debug("상태 변경: processId={}, status=COMPLETED", processId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(Long processId, String errorMessage) {
        AiProcessResult result = findResultById(processId);
        result.markAsFailed(errorMessage);
        resultRepository.save(result);
        log.debug("상태 변경: processId={}, status=FAILED, error={}", processId, errorMessage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setGeneratedFiles(Long processId, String orgKey, String shrKey, String scrKey) {
        AiProcessResult result = findResultById(processId);
        result.setGeneratedFiles(orgKey, shrKey, scrKey);
        resultRepository.save(result);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setTranscriptionResult(Long processId, List<ScriptSegment> rawTranscript, int playTime) {
        AiProcessResult result = findResultById(processId);
        result.setTranscriptionResult(rawTranscript, playTime);
        resultRepository.save(result);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setCorrectedScript(Long processId, List<ScriptSegment> correctedScript) {
        AiProcessResult result = findResultById(processId);
        result.setCorrectedScript(correctedScript);
        resultRepository.save(result);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setSuggestedMetadata(Long processId, String title, String summary) {
        AiProcessResult result = findResultById(processId);
        result.setSuggestedMetadata(title, summary);
        resultRepository.save(result);
    }

    private AiProcessResult findResultById(Long processId) {
        return resultRepository.findById(processId)
                .orElseThrow(() -> new IllegalArgumentException("처리 결과를 찾을 수 없습니다: " + processId));
    }
}
