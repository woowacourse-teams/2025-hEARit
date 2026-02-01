package com.onair.hearit.app.reaction.presentation;

import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.reaction.application.ReactionService;
import com.onair.hearit.core.domain.ReactionType;
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
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/hearits/{hearitId}/likes")
    public ResponseEntity<Void> createHearitLike(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal RequestUser requestUser) {
        reactionService.addReaction(requestUser.getUserInfo(), hearitId, ReactionType.LIKE);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/hearits/{hearitId}/likes")
    public ResponseEntity<Void> deleteHearitLike(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal RequestUser requestUser) {
        reactionService.removeReaction(requestUser.getUserInfo(), hearitId, ReactionType.LIKE);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
