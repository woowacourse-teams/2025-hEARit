package com.onair.hearit.app.presentation;

import com.onair.hearit.app.application.HearitExploreService;
import com.onair.hearit.app.application.HearitSearchService;
import com.onair.hearit.app.application.HearitService;
import com.onair.hearit.app.dto.request.CursorRequest;
import com.onair.hearit.app.dto.request.PagingRequest;
import com.onair.hearit.app.dto.response.CursorResponse;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.app.dto.response.HearitDetailResponse;
import com.onair.hearit.app.dto.response.HearitOfCategoryResponse;
import com.onair.hearit.app.dto.response.HearitSearchResponse;
import com.onair.hearit.app.dto.response.HearitsWithRecommendCategoryResponse;
import com.onair.hearit.app.dto.response.PagedResponse;
import com.onair.hearit.app.dto.response.RecommendHearitResponse;
import com.onair.hearit.auth.domain.UserContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearits")
public class HearitController {

    private final HearitService hearitService;
    private final HearitExploreService hearitExploreService;
    private final HearitSearchService hearitSearchService;

    @GetMapping("/{hearitId}")
    public ResponseEntity<HearitDetailResponse> readHearit(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal UserContext userContext) {
        HearitDetailResponse response = hearitService.getHearitDetail(hearitId, userContext);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/explore")
    public ResponseEntity<CursorResponse<ExploredHearitResponse>> readExploredHearits(
            @AuthenticationPrincipal UserContext userContext,
            @RequestParam(name = "cursorId", defaultValue = "0") long cursorId,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        CursorRequest cursorRequest = new CursorRequest(cursorId, size);
        CursorResponse<ExploredHearitResponse> responses = hearitExploreService.getExploredHearits(userContext,
                cursorRequest);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<RecommendHearitResponse>> readRecommendedHearits() {
        List<RecommendHearitResponse> responses = hearitService.getRecommendedHearits();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<HearitSearchResponse>> readSearchedHearits(
            @RequestParam(name = "searchTerm") String searchTerm,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UserContext userContext) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<HearitSearchResponse> response = hearitSearchService.search(searchTerm, pagingRequest,
                userContext);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommend-category")
    public ResponseEntity<List<HearitsWithRecommendCategoryResponse>> readHearitsWithRecommendCategory(
            @AuthenticationPrincipal UserContext userContext) {
        List<HearitsWithRecommendCategoryResponse> responses = hearitService.getHearitsWithRecommendCategory(
                userContext);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<HearitOfCategoryResponse>> readHearitsByCategory(
            @RequestParam(name = "categoryId") Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<HearitOfCategoryResponse> response = hearitService.getHearitsByCategory(categoryId,
                pagingRequest);
        return ResponseEntity.ok(response);
    }
}
