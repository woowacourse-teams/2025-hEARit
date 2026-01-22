package com.onair.hearit.app.like.presentation;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.like.application.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/hearits/{hearitId}/likes")
    public ResponseEntity<Void> createHearitLike(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal RequestUser requestUser) {
        likeService.addLike(requestUser.getUserInfo(), hearitId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/hearits/{hearitId}/likes")
    public ResponseEntity<Void> deleteHearitLike(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal RequestUser requestUser) {
        likeService.removeLike(requestUser.getUserInfo(), hearitId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
