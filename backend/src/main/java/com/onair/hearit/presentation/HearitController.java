package com.onair.hearit.presentation;

import com.onair.hearit.application.HearitService;
import com.onair.hearit.dto.request.HearitCreationRequest;
import com.onair.hearit.dto.response.HearitCreationResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hearit")
@RequiredArgsConstructor
public class HearitController {

    private final HearitService hearitService;

    @PostMapping
    public ResponseEntity<HearitCreationResponse> createHearit(@RequestBody HearitCreationRequest request) {
        HearitCreationResponse response = hearitService.addHearit(request);
        return ResponseEntity.created(URI.create("/hearit")).body(response);
    }
}
