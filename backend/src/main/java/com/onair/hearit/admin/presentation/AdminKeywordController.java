package com.onair.hearit.admin.presentation;

import com.onair.hearit.admin.application.AdminKeywordService;
import com.onair.hearit.admin.dto.request.KeywordCreateRequest;
import com.onair.hearit.admin.dto.response.KeywordInfoResponse;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Hidden;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<PagedResponse<KeywordInfoResponse>> readKeywords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<KeywordInfoResponse> response = adminKeywordService.getKeywords(pagingRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<KeywordInfoResponse>> readAllKeywords() {
        List<KeywordInfoResponse> response = adminKeywordService.getAllKeywords();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createKeyword(@RequestBody KeywordCreateRequest request) {
        adminKeywordService.addKeyword(request);
        return ResponseEntity.created(URI.create("/")).build();
    }
}
