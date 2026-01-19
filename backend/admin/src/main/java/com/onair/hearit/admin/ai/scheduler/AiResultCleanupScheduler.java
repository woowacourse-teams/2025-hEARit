package com.onair.hearit.admin.ai.scheduler;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.ai.domain.ProcessStatus;
import com.onair.hearit.admin.ai.infrastructure.jpa.AiProcessResultRepository;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiResultCleanupScheduler {

    private final AiProcessResultRepository resultRepository;
    private final FileStorage fileStorage;

    @Scheduled(cron = "0 0 5 * * *")
    @Transactional
    public void cleanupExpiredResults() {
        LocalDateTime now = LocalDateTime.now();
        log.info("AI 처리 결과 정리 스케줄러 시작: {}", now);
        List<AiProcessResult> expiredResults = resultRepository.findByStatusNotAndExpiresAtBefore(
                ProcessStatus.CONFIRMED, now);
        if (expiredResults.isEmpty()) {
            log.info("정리할 만료된 AI 처리 결과가 없습니다.");
            return;
        }
        log.info("정리 대상 AI 처리 결과: {}건", expiredResults.size());
        int successCount = 0;
        int failCount = 0;
        for (AiProcessResult result : expiredResults) {
            try {
                cleanupSingleResult(result);
                successCount++;
            } catch (Exception e) {
                log.error("AI 처리 결과 정리 실패 - id: {}, error: {}", result.getId(), e.getMessage());
                failCount++;
            }
        }
        log.info("AI 처리 결과 정리 완료 - 성공: {}건, 실패: {}건", successCount, failCount);
    }

    private void cleanupSingleResult(AiProcessResult result) {
        Long resultId = result.getId();
        log.debug("AI 처리 결과 정리 시작 - id: {}", resultId);
        deleteFileIfExists(result.getOriginalFileKey());
        deleteFileIfExists(result.getGeneratedOrgKey());
        deleteFileIfExists(result.getGeneratedShrKey());
        deleteFileIfExists(result.getGeneratedScrKey());
        resultRepository.delete(result);
        log.debug("AI 처리 결과 정리 완료 - id: {}", resultId);
    }

    private void deleteFileIfExists(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            fileStorage.deleteFile(key);
            log.debug("S3 파일 삭제 완료: {}", key);
        } catch (Exception e) {
            log.warn("S3 파일 삭제 실패 (무시됨): {}, error: {}", key, e.getMessage());
        }
    }
}
