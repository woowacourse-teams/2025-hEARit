package com.onair.hearit.app.explore.presentation;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.explore.dto.CursorRequest;
import com.onair.hearit.app.explore.dto.CursorResponseV1;
import com.onair.hearit.app.explore.dto.CursorResponseV2;
import com.onair.hearit.app.explore.application.HearitExploreService;
import com.onair.hearit.app.explore.dto.ExploredHearitResponse;
import com.onair.hearit.app.explore.dto.ExploredHearitResponseV3;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExploreController {

    private final HearitExploreService hearitExploreService;

    // V1: cursorId(auto-increment) 기반 페이지네이션, CursorResponseV1 래핑 (하위 호환용)
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

    // V2: cursorId(auto-increment) 기반 페이지네이션, CursorResponseV2 포맷 (hasNext 포함)
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

    // V3: score+hearitId 복합 커서 기반 페이지네이션
    // 변경점: cursorId(Long) → cursor(String, Base64 인코딩된 "score:hearitId")
    // 응답 필드: id, title, categoryColorCode, isBookmarked, bookmarkId, keywords, cursor
    @GetMapping("/api/v3/hearits/explore")
    public ResponseEntity<CursorResponseV2<ExploredHearitResponseV3>> readExploredHearitsV3(
            @AuthenticationPrincipal RequestUser requestUser,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        CursorResponseV2<ExploredHearitResponseV3> responses =
                hearitExploreService.getExploredHearitsV3(requestUser.getUserInfo(), cursor, size);
        return ResponseEntity.ok(responses);
    }
}
