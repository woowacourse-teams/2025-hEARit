package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitService;
import com.onair.hearit.dto.response.HearitExploreResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearits")
public class HearitController {

    private final HearitService hearitService;

    @GetMapping("/random")
    public ResponseEntity<List<HearitExploreResponse>> readExploredHearits() {
        return ResponseEntity.ok(hearitService.getExploredHearits());
    }
}
