package com.onair.hearit.hearit.presentation;

import com.onair.hearit.auth.domain.RequestUser;
import com.onair.hearit.common.dto.request.PagingRequest;
import com.onair.hearit.common.dto.response.PagedResponse;
import com.onair.hearit.hearit.application.HearitSearchService;
import com.onair.hearit.hearit.dto.HearitSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HearitSearchController {

    private final HearitSearchService hearitSearchService;

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
}
