package com.onair.hearit.admin.presentation.api;

import com.onair.hearit.admin.application.AdminRecommendHearitService;
import com.onair.hearit.admin.dto.request.AdminPagingRequest;
import com.onair.hearit.admin.dto.request.RecommendHearitCreateRequest;
import com.onair.hearit.admin.dto.request.RecommendHearitUpdateRequest;
import com.onair.hearit.admin.dto.response.AdminPagedResponse;
import com.onair.hearit.admin.dto.response.MonthlyRecommendHearitResponse;
import com.onair.hearit.admin.dto.response.AdminRecommendHearitResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/recommend")
public class AdminRecommendHearitController {

    private final AdminRecommendHearitService adminRecommendHearitService;

    @GetMapping("/hearits")
    public ResponseEntity<AdminPagedResponse<AdminRecommendHearitResponse>> readHearits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        AdminPagingRequest pagingRequest = new AdminPagingRequest(page, size);
        AdminPagedResponse<AdminRecommendHearitResponse> response = adminRecommendHearitService.getHearits(pagingRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MonthlyRecommendHearitResponse>> readRecommendedHearit(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        List<MonthlyRecommendHearitResponse> responses =
                adminRecommendHearitService.getMonthRecommendHearit(year, month);
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<Void> createRecommendHearit(@RequestBody RecommendHearitCreateRequest request) {
        adminRecommendHearitService.addRecommendHearits(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateRecommendHearit(@RequestBody RecommendHearitUpdateRequest request) {
        adminRecommendHearitService.modifyRecommendHearits(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
