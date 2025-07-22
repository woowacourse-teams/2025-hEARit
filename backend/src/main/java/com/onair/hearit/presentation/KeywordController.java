package com.onair.hearit.presentation;

import com.onair.hearit.application.KeywordService;
import com.onair.hearit.dto.response.KeywordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/keyword")
@Tag(name = "Keyword", description = "키워드")
public class KeywordController {

    private final KeywordService keywordService;

    @Operation(summary = "전체 키워드 목록 조회", description = "전체 키워드 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<KeywordResponse>> readKeywords() {
        List<KeywordResponse> responses = keywordService.getKeywords();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "단일 키워드 조회", description = "단일 키워드를 조회합니다.")
    @GetMapping("/{keywordId}")
    public ResponseEntity<KeywordResponse> readKeyword(@PathVariable Long keywordId) {
        KeywordResponse response = keywordService.getKeyword(keywordId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "오늘의 추천 키워드 조회", description = "오늘의 추천 키워드를 조회합니다.")
    @GetMapping("/recommend")
    public ResponseEntity<List<KeywordResponse>> readRecommendedKeywords(@RequestParam(defaultValue = "9") int size) {
        long todaySeed = LocalDate.now().toEpochDay();
        List<KeywordResponse> responses = keywordService.getRecommendedKeyword(todaySeed, size);
        return ResponseEntity.ok(responses);
    }
} 