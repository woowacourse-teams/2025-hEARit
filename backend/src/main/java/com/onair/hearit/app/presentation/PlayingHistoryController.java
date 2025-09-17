package com.onair.hearit.app.presentation;

import com.onair.hearit.app.application.PlayingHistoryService;
import com.onair.hearit.app.dto.request.PlayingHistoryRequest;
import com.onair.hearit.auth.domain.RequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/playing-histories")
public class PlayingHistoryController {

    private final PlayingHistoryService playingHistoryService;

    @PostMapping
    public ResponseEntity<Void> createPlayingHistory(
            @RequestBody PlayingHistoryRequest request,
            @AuthenticationPrincipal RequestUser requestUser) {
        playingHistoryService.addPlayingHistory(requestUser.getUserInfo(), request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
