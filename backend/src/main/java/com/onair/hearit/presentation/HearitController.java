package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitSearchService;
import com.onair.hearit.application.HearitService;
import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.dto.request.RandomHearitCondition;
import com.onair.hearit.dto.request.TitleSearchCondition;
import com.onair.hearit.dto.response.HearitDetailResponse;
import com.onair.hearit.dto.response.HearitSearchResponse;
import com.onair.hearit.dto.response.RandomHearitResponse;
import com.onair.hearit.dto.response.RecommendHearitResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Hearit", description = "히어릿")
public class HearitController {

    private final HearitService hearitService;
    private final HearitSearchService hearitSearchService;

    @Operation(summary = "단일 히어릿 상세 조회", description = "히어릿ID룰 통해 하나의 히어릿 상세 정보를 조회합니다.")
    @GetMapping("/{hearitId}")
    public ResponseEntity<HearitDetailResponse> readHearit(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal CurrentMember member) {
        //TODO : 추후 security url permit 적용
        Long memberId = extractMemberId(member);
        HearitDetailResponse response = hearitService.getHearitDetail(hearitId, memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "랜덤 히어릿 조회", description = "랜덤 히어릿을 page, size로 목록을 조회합니다.")
    @GetMapping("/random")
    public ResponseEntity<List<RandomHearitResponse>> readRandomHearits(
            @AuthenticationPrincipal CurrentMember member,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Long memberId = extractMemberId(member);
        RandomHearitCondition condition = new RandomHearitCondition(page, size);
        List<RandomHearitResponse> responses = hearitService.getRandomHearits(memberId, condition);
        return ResponseEntity.ok(responses);
    }

    private Long extractMemberId(CurrentMember member) {
        if (member == null) {
            return null;
        }
        return member.memberId();
    }

    @Operation(summary = "추천 히어릿 5개 조회", description = "추천 히어릿을 5개 조회합니다.")
    @GetMapping("/recommend")
    public ResponseEntity<List<RecommendHearitResponse>> readRecommendedHearits() {
        List<RecommendHearitResponse> responses = hearitService.getRecommendedHearits();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<HearitSearchResponse>> searchHearitsByTitle(
            @RequestParam(name = "searchTerm") String searchTerm,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        TitleSearchCondition condition = new TitleSearchCondition(searchTerm, page, size);
        List<HearitSearchResponse> response = hearitSearchService.searchByTitle(condition);
        return ResponseEntity.ok(response);
    }
}
