package com.onair.hearit.app.hearit.presentation;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.hearit.application.HearitService;
import com.onair.hearit.app.hearit.dto.HearitOverviewResponse;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse;
import com.onair.hearit.app.hearit.dto.HearitSortRequest;
import com.onair.hearit.app.hearit.dto.HearitsWithRecommendCategoryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HearitController {

    private final HearitService hearitService;

    @GetMapping("/api/v1/hearits/{hearitId}")
    public ResponseEntity<HearitDetailResponse> readHearit(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal RequestUser requestUser) {
        HearitDetailResponse response = hearitService.getHearitDetail(hearitId, requestUser.getUserInfo());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/hearits/recommend-category")
    public ResponseEntity<List<HearitsWithRecommendCategoryResponse>> readHearitsWithRecommendCategory(
            @AuthenticationPrincipal RequestUser requestUser) {
        List<HearitsWithRecommendCategoryResponse> responses =
                hearitService.getHearitsWithRecommendCategory(requestUser.getUserInfo());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/v1/hearits")
    public ResponseEntity<PagedResponse<HearitOverviewResponse>> readFilteredHearits(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") HearitSortRequest sortRequest,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal RequestUser requestUser) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<HearitOverviewResponse> responses = hearitService.getFilteredHearits(
                categoryId,
                sortRequest,
                requestUser.getUserInfo(),
                pagingRequest);
        return ResponseEntity.ok(responses);
    }
}
