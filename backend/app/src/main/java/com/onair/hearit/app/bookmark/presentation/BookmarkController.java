package com.onair.hearit.app.bookmark.presentation;

import com.onair.hearit.app.bookmark.application.BookmarkService;
import com.onair.hearit.app.bookmark.dto.BookmarkHearitResponseV1;
import com.onair.hearit.app.bookmark.dto.BookmarkHearitResponseV2;
import com.onair.hearit.app.bookmark.dto.BookmarkInfoResponse;
import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.auth.domain.RequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping("/api/v1/bookmarks/hearits")
    public ResponseEntity<PagedResponse<BookmarkHearitResponseV1>> readBookmarkHearitsV1(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal RequestUser requestUser) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        Page<BookmarkHearitResponseV2> v2Responses = bookmarkService.getBookmarkHearits(
                requestUser.getUserInfo(), pagingRequest);
        Page<BookmarkHearitResponseV1> v1Responses = v2Responses.map(BookmarkHearitResponseV1::from);
        return ResponseEntity.ok(PagedResponse.from(v1Responses));
    }

    @GetMapping("/api/v2/bookmarks/hearits")
    public ResponseEntity<PagedResponse<BookmarkHearitResponseV2>> readBookmarkHearitsV2(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal RequestUser requestUser) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        Page<BookmarkHearitResponseV2> bookmarkHearits = bookmarkService.getBookmarkHearits(
                requestUser.getUserInfo(),
                pagingRequest);
        return ResponseEntity.ok(PagedResponse.from(bookmarkHearits));
    }

    @PostMapping("/api/v1/bookmarks/hearits/{hearitId}")
    public ResponseEntity<BookmarkInfoResponse> createBookmark(
            @PathVariable Long hearitId,
            @AuthenticationPrincipal RequestUser requestUser) {
        BookmarkInfoResponse response = bookmarkService.addBookmark(requestUser.getUserInfo(), hearitId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/v1/bookmarks/{bookmarkId}")
    public ResponseEntity<Void> deleteBookmark(
            @PathVariable Long bookmarkId,
            @AuthenticationPrincipal RequestUser requestUser) {
        bookmarkService.deleteBookmark(bookmarkId, requestUser.getUserInfo());
        return ResponseEntity.noContent().build();
    }
}
