package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.HearitFileUpdateRequest;
import com.onair.hearit.admin.dto.request.PresignedUrlRequest;
import com.onair.hearit.admin.dto.response.FilesPresignedUrlResponse;
import com.onair.hearit.admin.dto.response.PresignedUrlResponse;
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

    public FilesPresignedUrlResponse getFilesPresignedUrl(PresignedUrlRequest request) {
        PresignedUrlResponse originalAudioPresignedUrl = createPutUrl(FileType.ORIGINAL,
                request.originalAudioFileName());
        PresignedUrlResponse shortAudiosPresignedUrl = createPutUrl(FileType.SHORT, request.shortAudioFileName());
        PresignedUrlResponse scriptPresignedUrl = createPutUrl(FileType.SCRIPT, request.scriptFileName());
        return new FilesPresignedUrlResponse(originalAudioPresignedUrl, shortAudiosPresignedUrl, scriptPresignedUrl);
    }

    private PresignedUrlResponse createPutUrl(FileType fileType, String fileName) {
        fileType.validateFileName(fileName);
        String key = fileType.generateKey(fileName);
        URL presignedUrl = fileStorage.createPutUrl(key);
        return new PresignedUrlResponse(key, presignedUrl);
    }

    @Transactional
    public void modifyHearitFile(Long hearitId, HearitFileUpdateRequest request, FileType fileType) {
        Hearit hearit = getHearitById(hearitId);
        fileStorage.deleteFile(hearit.getFileUrl(fileType));
        String uploadFilePath = fileStorage.uploadFile(request.file(), fileType);
        hearit.updateFileUrl(uploadFilePath, fileType);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findByIdWithCategoryAndSources(hearitId)
                .orElseThrow(() -> new AdminNotFoundException("hearitId", hearitId.toString()));
    }
}
