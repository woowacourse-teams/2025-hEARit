package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitSearchService;
import com.onair.hearit.application.HearitService;
import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.GroupedHearitsWithCategoryResponse;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.PagedResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
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
    private final HearitSearchService hearitSearchService;

    @GetMapping("/{hearitId}")
    public ResponseEntity<HearitDetailResponse> readHearit(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal CurrentMember member) {
        Long memberId = extractMemberId(member);
        HearitDetailResponse response = hearitService.getHearitDetail(hearitId, memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/random")
    public ResponseEntity<PagedResponse<RandomHearitResponse>> readRandomHearits(
            @AuthenticationPrincipal CurrentMember member,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Long memberId = extractMemberId(member);
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<RandomHearitResponse> responses = hearitService.getRandomHearits(memberId, pagingRequest);
        return ResponseEntity.ok(responses);
    }

    private Long extractMemberId(CurrentMember member) {
        if (member == null) {
            return null;
        }
        return member.memberId();
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<RecommendHearitResponse>> readRecommendedHearits() {
        List<RecommendHearitResponse> responses = hearitService.getRecommendedHearits();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<HearitSearchResponse>> searchHearitsByTitle(
            @RequestParam(name = "searchTerm") String searchTerm,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<HearitSearchResponse> response = hearitSearchService.search(searchTerm, pagingRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리별로 그룹화된 히어릿들 조회", description = "고정된 3개 카테고리별로 최신 히어릿 5개를 반환합니다.")
    @GetMapping("/grouped-by-category")
    public ResponseEntity<List<GroupedHearitsWithCategoryResponse>> readHomeHearits() {
        List<GroupedHearitsWithCategoryResponse> responses = hearitService.getGroupedHearitsByCategory();
        return ResponseEntity.ok(responses);
    }
}
