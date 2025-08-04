package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.application.AdminHearitService;
import com.onair.hearit.admin.dto.request.HearitCreateRequest;
import com.onair.hearit.admin.dto.request.HearitFileUpdateRequest;
import com.onair.hearit.admin.dto.request.HearitMetaDataUpdateRequest;
import com.onair.hearit.admin.dto.response.HearitAdminResponse;
import com.onair.hearit.domain.FileType;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Hidden;
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

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminHearitController {

    private final AdminHearitService adminHearitService;

    @GetMapping("/hearits")
    public ResponseEntity<PagedResponse<HearitAdminResponse>> readHearits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<HearitAdminResponse> response = adminHearitService.getHearits(pagingRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/hearits", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createHearit(@ModelAttribute @Valid HearitCreateRequest request) {
        adminHearitService.addHearit(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/hearits/{hearitId}")
    public ResponseEntity<Void> updateHearitById(
            @PathVariable Long hearitId,
            @RequestBody @Valid HearitMetaDataUpdateRequest request) {
        adminHearitService.modifyHearitMetaData(hearitId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/hearits/{hearitId}/original-audio")
    public ResponseEntity<Void> updateHearitOriginalAudio(
            @PathVariable Long hearitId,
            @ModelAttribute @Valid HearitFileUpdateRequest request) {
        adminHearitService.modifyHearitFile(hearitId, request, FileType.ORIGINAL);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/hearits/{hearitId}/short-audio")
    public ResponseEntity<Void> updateHearitShortAudio(
            @PathVariable Long hearitId,
            @ModelAttribute @Valid HearitFileUpdateRequest request) {
        adminHearitService.modifyHearitFile(hearitId, request, FileType.SHORT);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/hearits/{hearitId}/script")
    public ResponseEntity<Void> updateHearitScript(
            @PathVariable Long hearitId,
            @ModelAttribute @Valid HearitFileUpdateRequest request) {
        adminHearitService.modifyHearitFile(hearitId, request, FileType.SCRIPT);
        return ResponseEntity.noContent().build();
    }
}
