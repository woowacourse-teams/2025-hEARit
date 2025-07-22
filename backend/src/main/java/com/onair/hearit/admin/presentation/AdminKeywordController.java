package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.application.AdminKeywordService;
import com.onair.hearit.admin.dto.request.KeywordCreateRequest;
import com.onair.hearit.admin.dto.response.KeywordInfoResponse;
import com.onair.hearit.admin.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Hidden;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/keywords")
public class AdminKeywordController {

    private final AdminKeywordService adminKeywordService;

    @GetMapping
    public ResponseEntity<PagedResponse<KeywordInfoResponse>> getPageKeywords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        PagedResponse<KeywordInfoResponse> response = adminKeywordService.getPageKeywords(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<KeywordInfoResponse> createKeyword(KeywordCreateRequest request) {
        KeywordInfoResponse response = adminKeywordService.createKeyword(request);
        return ResponseEntity.created(URI.create("/")).body(response);
    }
}
