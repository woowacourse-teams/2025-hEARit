package com.onair.hearit.presentation;

import com.onair.hearit.application.BookmarkService;
import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.dto.response.BookmarkHearitResponse;
import com.onair.hearit.dto.response.HearitDetailResponse;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hearits/")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping("/bookmarks")
    public ResponseEntity<List<BookmarkHearitResponse>> readBookmarkHearits(@AuthenticationPrincipal CurrentMember member) {
        List<BookmarkHearitResponse> responses = bookmarkService.getBookmarkHearits(member.memberId());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{hearitId}/bookmarks")
    public ResponseEntity<Void> createBookmark(@PathVariable Long hearitId,
                                               @AuthenticationPrincipal CurrentMember member) {
        bookmarkService.addBookmark(hearitId, member.memberId());
        return ResponseEntity.created(URI.create("/api/v1/hearits/" + hearitId)).build();
    }

    @DeleteMapping("/{hearitId}/bookmarks/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long bookmarkId,
                                               @AuthenticationPrincipal CurrentMember member) {
        bookmarkService.deleteBookmark(bookmarkId, member.memberId());
        return ResponseEntity.noContent().build();
    }
}
