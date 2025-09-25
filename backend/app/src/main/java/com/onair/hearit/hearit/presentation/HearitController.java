package com.onair.hearit.hearit.presentation;

import com.onair.hearit.hearit.application.HearitSearchService;
import com.onair.hearit.hearit.application.HearitService;
import com.onair.hearit.explore.application.HearitExploreService;
import com.onair.hearit.common.dto.request.CursorRequest;
import com.onair.hearit.common.dto.request.PagingRequest;
import com.onair.hearit.common.dto.response.CursorResponseV1;
import com.onair.hearit.common.dto.response.CursorResponseV2;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.hearit.dto.HearitDetailResponse;
import com.onair.hearit.hearit.dto.HearitOfCategoryResponse;
import com.onair.hearit.hearit.dto.HearitSearchResponse;
import com.onair.hearit.hearit.dto.HearitsWithRecommendCategoryResponse;
import com.onair.hearit.common.dto.response.PagedResponse;
import com.onair.hearit.recommendhearit.dto.RecommendHearitResponse;
import com.onair.hearit.auth.domain.RequestUser;
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
    private final HearitExploreService hearitExploreService;
    private final HearitSearchService hearitSearchService;

    @GetMapping("/api/v1/hearits/{hearitId}")
    public ResponseEntity<HearitDetailResponse> readHearit(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal RequestUser requestUser) {
        HearitDetailResponse response = hearitService.getHearitDetail(hearitId, requestUser.getUserInfo());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/hearits/explore")
    public ResponseEntity<CursorResponseV1<ExploredHearitResponse>> readExploredHearitsV1(
            @AuthenticationPrincipal RequestUser requestUser,
            @RequestParam(name = "cursorId", defaultValue = "0") long cursorId,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        CursorRequest cursorRequest = new CursorRequest(cursorId, size);
        CursorResponseV2<ExploredHearitResponse> responses =
                hearitExploreService.getExploredHearits(requestUser.getUserInfo(), cursorRequest);
        CursorResponseV1<ExploredHearitResponse> responsesV1 = CursorResponseV1.from(responses);
        return ResponseEntity.ok(responsesV1);
    }

    @GetMapping("/api/v2/hearits/explore")
    public ResponseEntity<CursorResponseV2<ExploredHearitResponse>> readExploredHearitsV2(
            @AuthenticationPrincipal RequestUser requestUser,
            @RequestParam(name = "cursorId", defaultValue = "0") long cursorId,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        CursorRequest cursorRequest = new CursorRequest(cursorId, size);
        CursorResponseV2<ExploredHearitResponse> responses =
                hearitExploreService.getExploredHearits(requestUser.getUserInfo(), cursorRequest);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/v1/hearits/recommend")
    public ResponseEntity<List<RecommendHearitResponse>> readRecommendedHearits() {
        List<RecommendHearitResponse> responses = hearitService.getRecommendedHearits();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/v1/hearits/search")
    public ResponseEntity<PagedResponse<HearitSearchResponse>> readSearchedHearits(
            @RequestParam(name = "searchTerm") String searchTerm,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal RequestUser requestUser) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<HearitSearchResponse> response =
                hearitSearchService.search(searchTerm, pagingRequest, requestUser.getUserInfo());
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
