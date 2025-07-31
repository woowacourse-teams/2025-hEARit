package com.onair.hearit.presentation;

import com.onair.hearit.application.KeywordService;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.KeywordResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/keywords")
public class KeywordController {

    private final KeywordService keywordService;

    @GetMapping
    public ResponseEntity<List<KeywordResponse>> readKeywords(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        List<KeywordResponse> responses = keywordService.getKeywords(pagingRequest);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{keywordId}")
    public ResponseEntity<KeywordResponse> readKeyword(@PathVariable Long keywordId) {
        KeywordResponse response = keywordService.getKeyword(keywordId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<KeywordResponse>> readRecommendedKeywords(
            @RequestParam(name = "size", defaultValue = "9") int size) {
        List<KeywordResponse> responses = keywordService.getRecommendedKeywords(size);
        return ResponseEntity.ok(responses);
    }
}
