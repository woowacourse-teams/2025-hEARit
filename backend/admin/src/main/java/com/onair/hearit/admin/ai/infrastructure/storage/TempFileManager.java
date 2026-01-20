package com.onair.hearit.admin.ai.infrastructure.storage;

import com.onair.hearit.admin.ai.domain.AiProcessResult;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TempFileManager {

    private final FileStorage fileStorage;

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

    public void cleanupTempFiles(AiProcessResult result) {
        deleteIfExists(result.getOriginalFileKey());
        deleteIfExists(result.getGeneratedOrgKey());
        deleteIfExists(result.getGeneratedShrKey());
        deleteIfExists(result.getGeneratedScrKey());
    }
}
