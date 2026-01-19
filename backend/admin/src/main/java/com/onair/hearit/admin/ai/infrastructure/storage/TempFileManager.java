package com.onair.hearit.admin.ai.infrastructure.storage;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 처리 과정에서 생성되는 임시 파일 관리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TempFileManager {

    private final FileStorage fileStorage;

    /**
     * S3 파일 삭제 (존재하지 않아도 예외 발생하지 않음)
     *
     * @param key S3 파일 키
     */
    public void deleteIfExists(String key) {
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

    /**
     * AI 처리 결과와 관련된 모든 임시 파일 삭제
     *
     * @param result AI 처리 결과
     */
    public void cleanupTempFiles(AiProcessResult result) {
        deleteIfExists(result.getOriginalFileKey());
        deleteIfExists(result.getGeneratedOrgKey());
        deleteIfExists(result.getGeneratedShrKey());
        deleteIfExists(result.getGeneratedScrKey());
    }
}
