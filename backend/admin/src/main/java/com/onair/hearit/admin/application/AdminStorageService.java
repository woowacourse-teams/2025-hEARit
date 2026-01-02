package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.HearitFileUpdateRequest;
import com.onair.hearit.admin.dto.request.UploadUrlRequest;
import com.onair.hearit.admin.dto.response.FilesUploadUrlResponse;
import com.onair.hearit.admin.dto.response.UploadUrlResponse;
import com.onair.hearit.admin.exception.custom.AdminNotFoundException;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.core.domain.FileType;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStorageService {

    private final HearitRepository hearitRepository;
    private final FileStorage fileStorage;

    public FilesUploadUrlResponse getFilesUploadUrl(UploadUrlRequest request) {
        UploadUrlResponse originalAudioUploadUrl = getUploadUrl(FileType.ORIGINAL, request.originalAudioFileName());
        UploadUrlResponse shortAudiosUploadUrl = getUploadUrl(FileType.SHORT, request.shortAudioFileName());
        UploadUrlResponse scriptUploadUrl = getUploadUrl(FileType.SCRIPT, request.scriptFileName());
        return new FilesUploadUrlResponse(originalAudioUploadUrl, shortAudiosUploadUrl, scriptUploadUrl);
    }

    private UploadUrlResponse getUploadUrl(FileType fileType, String fileName) {
        fileType.validateFileName(fileName);
        String key = fileType.generateKey(fileName);
        URL uploadUrl = fileStorage.createPutUrl(key);
        return new UploadUrlResponse(key, uploadUrl);
    }

    @Transactional
    public void modifyHearitFile(Long hearitId, HearitFileUpdateRequest request, FileType fileType) {
        Hearit hearit = getHearitById(hearitId);
        String uploadFilePath = fileStorage.uploadFile(request.file(), fileType);
        hearit.updateFileUrl(uploadFilePath, fileType);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findByIdWithCategoryAndSources(hearitId)
                .orElseThrow(() -> new AdminNotFoundException("hearitId", hearitId.toString()));
    }
}
