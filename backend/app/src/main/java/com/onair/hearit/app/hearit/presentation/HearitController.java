package com.onair.hearit.app.hearit.presentation;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.hearit.application.HearitService;
import com.onair.hearit.app.hearit.dto.HearitDetailResponse;
import com.onair.hearit.app.hearit.dto.HearitOfCategoryResponse;
import com.onair.hearit.app.hearit.dto.RecentHearitResponse;
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

    @GetMapping("/api/v1/hearits/recent")
    public ResponseEntity<List<RecentHearitResponse>> readRecentHearit(@AuthenticationPrincipal RequestUser requestUser) {
        List<RecentHearitResponse> responses = hearitService.getRecentHearits(requestUser.getUserInfo());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/v1/hearits")
    public ResponseEntity<PagedResponse<HearitOfCategoryResponse>> readHearitsByCategory(
            @RequestParam(name = "categoryId") Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal RequestUser requestUser) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<HearitOfCategoryResponse> response = hearitService.getHearitsByCategory(categoryId,
                pagingRequest, requestUser.getUserInfo());
        return ResponseEntity.ok(response);
    }
}
