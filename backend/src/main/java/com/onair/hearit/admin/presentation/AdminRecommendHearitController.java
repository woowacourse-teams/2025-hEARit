package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.application.AdminRecommendHearitService;
import com.onair.hearit.admin.dto.request.RecommendHearitCreateRequest;
import com.onair.hearit.admin.dto.request.RecommendHearitUpdateRequest;
import com.onair.hearit.admin.dto.response.MonthlyRecommendedHearitResponse;
import com.onair.hearit.admin.dto.response.RecommendHearitResponse;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.PagedResponse;
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

//TODO 테스트 작성
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/recommend-hearits")
public class AdminRecommendHearitController {

    private final AdminRecommendHearitService adminRecommendHearitService;

    @GetMapping("/hearits")
    public ResponseEntity<PagedResponse<RecommendHearitResponse>> readHearits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<RecommendHearitResponse> response = adminRecommendHearitService.getHearits(pagingRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MonthlyRecommendedHearitResponse>> readRecommendedHearit(
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        List<MonthlyRecommendedHearitResponse> responses =
                adminRecommendHearitService.getMonthRecommendedHearit(year, month);
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
