package com.onair.hearit.playinghistory.presentation;

import com.onair.hearit.playinghistory.application.PlayingHistoryService;
import com.onair.hearit.playinghistory.dto.PlayingHistoryRequest;
import com.onair.hearit.playinghistory.dto.RecentlyPlayedHearitResponse;
import com.onair.hearit.auth.domain.RequestUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/playing-histories")
public class PlayingHistoryController {

    private final PlayingHistoryService playingHistoryService;

    @GetMapping("/hearits")
    public ResponseEntity<List<RecentlyPlayedHearitResponse>> readPlayingHistories(
            @AuthenticationPrincipal RequestUser requestUser) {
        List<RecentlyPlayedHearitResponse> responses =
                playingHistoryService.getRecentPlayingHistory(requestUser.getUserInfo());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<Void> createPlayingHistory(
            @RequestBody PlayingHistoryRequest request,
            @AuthenticationPrincipal RequestUser requestUser) {
        playingHistoryService.addPlayingHistory(requestUser.getUserInfo(), request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
