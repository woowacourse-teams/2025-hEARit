package com.onair.hearit.admin.presentation.api;

import com.onair.hearit.admin.application.AdminStorageService;
import com.onair.hearit.admin.dto.request.HearitFileUpdateRequest;
import com.onair.hearit.admin.dto.request.UploadUrlRequest;
import com.onair.hearit.admin.dto.response.FilesUploadUrlResponse;
import com.onair.hearit.core.domain.FileType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminStorageController {

    private final AdminStorageService adminStorageService;

    @PostMapping("/storage/upload-urls")
    public ResponseEntity<FilesUploadUrlResponse> createFilesUploadUrl(
            @RequestBody @Valid UploadUrlRequest request) {
        FilesUploadUrlResponse response = adminStorageService.getFilesUploadUrl(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/hearits/{hearitId}/original-audio")
    public ResponseEntity<Void> updateHearitOriginalAudio(
            @PathVariable Long hearitId,
            @ModelAttribute @Valid HearitFileUpdateRequest request) {
        adminStorageService.modifyHearitFile(hearitId, request, FileType.ORIGINAL);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/hearits/{hearitId}/short-audio")
    public ResponseEntity<Void> updateHearitShortAudio(
            @PathVariable Long hearitId,
            @ModelAttribute @Valid HearitFileUpdateRequest request) {
        adminStorageService.modifyHearitFile(hearitId, request, FileType.SHORT);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/hearits/{hearitId}/script")
    public ResponseEntity<Void> updateHearitScript(
            @PathVariable Long hearitId,
            @ModelAttribute @Valid HearitFileUpdateRequest request) {
        adminStorageService.modifyHearitFile(hearitId, request, FileType.SCRIPT);
        return ResponseEntity.noContent().build();
    }
}
