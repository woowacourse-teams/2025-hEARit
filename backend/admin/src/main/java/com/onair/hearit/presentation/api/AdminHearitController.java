package com.onair.hearit.presentation.api;

import com.onair.hearit.application.AdminHearitService;
import com.onair.hearit.dto.request.AdminPagingRequest;
import com.onair.hearit.dto.request.HearitCreateRequest;
import com.onair.hearit.dto.request.HearitFileUpdateRequest;
import com.onair.hearit.dto.request.HearitInfoUpdateRequest;
import com.onair.hearit.dto.response.AdminHearitResponse;
import com.onair.hearit.dto.response.AdminPagedResponse;
import com.onair.hearit.domain.FileType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/hearits")
public class AdminHearitController {

    private final AdminHearitService adminHearitService;

    @GetMapping
    public ResponseEntity<AdminPagedResponse<AdminHearitResponse>> readHearits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        AdminPagingRequest pagingRequest = new AdminPagingRequest(page, size);
        AdminPagedResponse<AdminHearitResponse> response = adminHearitService.getHearits(pagingRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createHearit(@ModelAttribute @Valid HearitCreateRequest request) {
        adminHearitService.addHearit(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{hearitId}")
    public ResponseEntity<Void> updateHearitById(
            @PathVariable Long hearitId,
            @RequestBody @Valid HearitInfoUpdateRequest request) {
        adminHearitService.modifyHearitMetaData(hearitId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{hearitId}/original-audio")
    public ResponseEntity<Void> updateHearitOriginalAudio(
            @PathVariable Long hearitId,
            @ModelAttribute @Valid HearitFileUpdateRequest request) {
        adminHearitService.modifyHearitFile(hearitId, request, FileType.ORIGINAL);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{hearitId}/short-audio")
    public ResponseEntity<Void> updateHearitShortAudio(
            @PathVariable Long hearitId,
            @ModelAttribute @Valid HearitFileUpdateRequest request) {
        adminHearitService.modifyHearitFile(hearitId, request, FileType.SHORT);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{hearitId}/script")
    public ResponseEntity<Void> updateHearitScript(
            @PathVariable Long hearitId,
            @ModelAttribute @Valid HearitFileUpdateRequest request) {
        adminHearitService.modifyHearitFile(hearitId, request, FileType.SCRIPT);
        return ResponseEntity.noContent().build();
    }
}
