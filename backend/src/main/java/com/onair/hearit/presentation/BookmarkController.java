package com.onair.hearit.presentation;

import com.onair.hearit.application.BookmarkService;
import com.onair.hearit.auth.dto.UserContext;
import com.onair.hearit.dto.request.PagingRequest;
import com.onair.hearit.dto.response.BookmarkHearitResponse;
import com.onair.hearit.dto.response.BookmarkInfoResponse;
import com.onair.hearit.dto.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping("/hearits")
    public ResponseEntity<PagedResponse<BookmarkHearitResponse>> readBookmarkHearits(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UserContext userContext) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<BookmarkHearitResponse> responses = bookmarkService.getBookmarkHearits(userContext,
                pagingRequest);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/hearits/{hearitId}")
    public ResponseEntity<BookmarkInfoResponse> createBookmark(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal UserContext userContext) {
        BookmarkInfoResponse response = bookmarkService.addBookmark(userContext, hearitId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            @PathVariable Long bookmarkId,
            @AuthenticationPrincipal UserContext userContext) {
        bookmarkService.deleteBookmark(bookmarkId, userContext);
        return ResponseEntity.noContent().build();
    }
}
